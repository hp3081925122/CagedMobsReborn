package com.corgam.cagedmobsreborn.blocks.mob_cage;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.helpers.AdaptedItemHandler;
import com.corgam.cagedmobsreborn.items.upgrades.SpeedIIIUpgradeItem;
import com.corgam.cagedmobsreborn.items.upgrades.SpeedIIUpgradeItem;
import com.corgam.cagedmobsreborn.items.upgrades.SpeedIUpgradeItem;
import com.corgam.cagedmobsreborn.items.upgrades.UpgradeItem;
import com.corgam.cagedmobsreborn.registers.CagedBlockEntities;
import com.corgam.cagedmobsreborn.registers.CagedItems;
import com.corgam.cagedmobsreborn.serializers.AutoEntityDataHelper;
import com.corgam.cagedmobsreborn.serializers.RecipesHelper;
import com.corgam.cagedmobsreborn.serializers.SerializationHelper;
import com.corgam.cagedmobsreborn.serializers.entity.AdditionalLootData;
import com.corgam.cagedmobsreborn.serializers.entity.EntityData;
import com.corgam.cagedmobsreborn.serializers.entity.LootData;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageBlock.HOPPING;
import static com.corgam.cagedmobsreborn.helpers.UpgradeItemsParticles.*;

public class MobCageBlockEntity extends BlockEntity {

    // 生物与环境缓存数据
    private EnvironmentData environmentData = null;
    private EntityData entity = null;
    private EntityType<?> entityType = null;
    private Entity cachedEntity;
    private SpawnData renderedEntity;
    private Entity lootEntity;
    // 生长进度
    private int currentGrowTicks = 0;
    private int totalGrowTicks = 0;
    private boolean waitingForHarvest = false;
    // 羊颜色等实体颜色标记
    private int color = 0;
    // 存储标签名
    public static final String ITEMS_TAG = "Inventory";
    // 槽位配置
    public static int UPGRADES_COUNT = 3;
    public static int ENVIRONMENT_SLOT = 0;
    public static int SLOT_COUNT = UPGRADES_COUNT + 1;
    // 物品传输与存储句柄（NeoForge Transfer API v2）
    private boolean droppingInventory = false;
    private final ItemStacksResourceHandler items = createItemHandler();
    private final ResourceHandler<ItemResource> restrictedItemHandler = new AdaptedItemHandler(items) {
        // 禁止外部自动化设备（如漏斗）抽取内部环境与升级
        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        // 禁止外部自动化设备（如漏斗）向内部槽位插入物品
        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    };

    public MobCageBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(CagedBlockEntities.MOB_CAGE_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    @Nonnull
    private ItemStacksResourceHandler createItemHandler() {
        return new ItemStacksResourceHandler(SLOT_COUNT) {
            @Override
            public boolean isValid(int index, ItemResource resource) {
                if (resource.isEmpty()) {
                    return false;
                }
                if (index == ENVIRONMENT_SLOT) {
                    return existsEnvironmentFromItemStack(resource.toStack());
                }
                return resource.getItem() instanceof UpgradeItem;
            }

            @Override
            protected int getCapacity(int index, ItemResource resource) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int index, ItemStack previousContents) {
                if (droppingInventory) {
                    return;
                }
                // 更新环境或生长周期
                if (index == ENVIRONMENT_SLOT) {
                    updateEnvironment();
                } else {
                    calculateTotalGrowTicks();
                }
                // 通知服务端保存并同步客户端
                setChanged();
                if (level != null) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
                }
            }
        };
    }

    private ItemStack getStackInSlot(int slot) {
        int count = this.items.getAmountAsInt(slot);
        return count <= 0 ? ItemStack.EMPTY : this.items.getResource(slot).toStack(count);
    }

    public ItemStacksResourceHandler getInventoryHandler() {
        return this.items;
    }

    public ResourceHandler<ItemResource> getRestrictedItemHandler() {
        return this.restrictedItemHandler;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MobCageBlockEntity blockEntity) {
        // 仅当同时存在环境与生物且未等待收获时推进生长
        if (blockEntity.hasEnvAndEntity() && !blockEntity.waitingForHarvest) {
            if (blockEntity.currentGrowTicks >= blockEntity.totalGrowTicks) {
                blockEntity.attemptHarvest(state);
            } else {
                if (!blockEntity.entity.ifRequiresWater() || blockEntity.getBlockState().getValue(BlockStateProperties.WATERLOGGED)) {
                    blockEntity.currentGrowTicks++;
                }
            }
        }
        // 客户端发射升级粒子效果
        if (level != null && level.isClientSide()) {
            if (CagedMobsReborn.CLIENT_CONFIG.shouldUpgradesParticles()) {
                for (ItemStack upgrade : blockEntity.getUpgradesAsItemStacks()) {
                    blockEntity.emitUpgradeParticles(upgrade, blockEntity);
                }
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && !this.level.isClientSide()) {
            CagedMobsReborn.LOGGER.debug("Dropping cage inventory before block entity removal at {}: {}", pos, this.getUpgradesAsItemStacks());
            this.dropInventory();
        }
    }

    public void dropInventory() {
        this.droppingInventory = true;
        try {
            for (int i = 0; i < this.items.size(); i++) {
                ItemStack stack = this.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    this.dropItem(stack);
                    this.items.set(i, ItemResource.EMPTY, 0);
                }
            }
        } finally {
            this.droppingInventory = false;
        }
    }

    private void dropItem(ItemStack item) {
        if (this.level != null && !this.level.isClientSide() && !item.isEmpty()) {
            final double offsetX = (double) (level.getRandom().nextFloat() * 0.7F) + (double) 0.15F;
            final double offsetY = (double) (level.getRandom().nextFloat() * 0.7F) + (double) 0.060000002F + 0.6D;
            final double offsetZ = (double) (level.getRandom().nextFloat() * 0.7F) + (double) 0.15F;
            final ItemEntity itemEntity = new ItemEntity(this.level, this.worldPosition.getX() + offsetX, this.worldPosition.getY() + offsetY, this.worldPosition.getZ() + offsetZ, item);
            itemEntity.setDefaultPickUpDelay();
            this.level.addFreshEntity(itemEntity);
        }
    }

    // 环境相关逻辑

    public void setEnvironment(ItemStack heldItem) {
        this.environmentData = getEnvironmentDataFromItemStack(heldItem);
        if (this.items.getAmountAsInt(ENVIRONMENT_SLOT) == 0 && !heldItem.isEmpty()) {
            // 写入环境物品到环境槽位
            this.items.set(ENVIRONMENT_SLOT, ItemResource.of(heldItem), 1);
        }
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static EnvironmentData getEnvironmentDataFromItemStack(ItemStack heldItem) {
        EnvironmentData finalEnvData = null;
        for (final EnvironmentData recipe : RecipesHelper.getEnvironmentRecipesList()) {
            if (recipe != null) {
                if (recipe.getInputItem().test(heldItem)) {
                    finalEnvData = recipe;
                    break;
                }
            }
        }
        return finalEnvData;
    }

    public void removeEnvironment() {
        this.environmentData = null;
        this.removeEntity();
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void updateEnvironment() {
        ItemStack envItem = this.getStackInSlot(ENVIRONMENT_SLOT);
        if (envItem.isEmpty()) {
            this.removeEnvironment();
        } else if (this.environmentData == null || !this.environmentData.getInputItem().test(envItem)) {
            this.setEnvironment(envItem);
        }
    }

    public EnvironmentData getEnvironmentData() {
        if (this.environmentData == null && this.hasEnvironment()) {
            this.environmentData = getEnvironmentDataFromItemStack(this.getStackInSlot(ENVIRONMENT_SLOT));
        }
        return this.environmentData;
    }

    public ItemStack getEnvironmentItemStack() {
        return this.getStackInSlot(ENVIRONMENT_SLOT);
    }

    public boolean hasEnvironment() {
        return this.items.getAmountAsInt(ENVIRONMENT_SLOT) > 0;
    }

    public static boolean existsEnvironmentFromItemStack(ItemStack heldItem) {
        if (heldItem.isEmpty()) {
            return false;
        }
        for (final Recipe<?> recipe : RecipesHelper.getEnvironmentRecipesList()) {
            if (recipe instanceof EnvironmentData envData) {
                if (envData.getInputItem().test(heldItem)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isEnvironmentSuitable(Player player, EntityType<?> entityType, BlockState state) {
        EntityData recipe = getMobDataFromType(entityType);
        if (recipe == null) {
            return false;
        }
        if (recipe.ifRequiresWater() && !state.getValue(BlockStateProperties.WATERLOGGED)) {
            player.sendOverlayMessage(Component.translatable("block.cagedmobsreborn.mob_cage.requiresWater").withStyle(ChatFormatting.RED));
            return false;
        }
        boolean universal = false;
        if (this.environmentData != null) {
            universal = this.environmentData.getCategories().contains(RecipesHelper.UNIVERSAL_ENVIRONMENT);
            if (universal && RecipesHelper.isUniversalEnvironmentAllowed(entityType)) {
                return true;
            }
            for (String env : this.environmentData.getCategories()) {
                if (recipe.getEnvironments().contains(env)) {
                    return true;
                }
            }
        }
        if (universal) {
            player.sendOverlayMessage(Component.translatable("block.cagedmobsreborn.mob_cage.universalBlacklisted").withStyle(ChatFormatting.RED));
            return false;
        }
        player.sendOverlayMessage(Component.translatable("block.cagedmobsreborn.mob_cage.envNotSuitable").withStyle(ChatFormatting.RED));
        return false;
    }

    // 生物相关逻辑

    public void setEntityFromSampler(EntityType<?> entityType, ItemStack sampler) {
        this.color = 0;
        if (entityType.toString().contains("sheep")) {
            if (SerializationHelper.hasCustomData(sampler) && SerializationHelper.getCustomData(sampler).contains("Color")) {
                this.color = SerializationHelper.getCustomData(sampler).getIntOr("Color", 0);
            }
        }
        this.entity = getMobDataFromType(entityType);
        this.entityType = entityType;
        this.cachedEntity = null;
        this.renderedEntity = null;
        this.lootEntity = null;
        this.totalGrowTicks = this.calculateTotalGrowTicks();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
        this.setChanged();
    }

    public Optional<EntityData> getEntity() {
        if (this.entity == null && this.entityType != null) {
            this.entity = getMobDataFromType(this.entityType);
            if (this.entity != null && this.totalGrowTicks == 0) {
                this.totalGrowTicks = this.calculateTotalGrowTicks();
            }
        }
        return Optional.ofNullable(this.entity);
    }

    public EntityType<?> getEntityType() {
        return this.entityType;
    }

    public void removeEntity() {
        this.entity = null;
        this.entityType = null;
        this.cachedEntity = null;
        this.renderedEntity = null;
        this.lootEntity = null;
        this.currentGrowTicks = 0;
        this.totalGrowTicks = 0;
        this.waitingForHarvest = false;
        this.color = 0;
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
        this.setChanged();
    }

    public boolean hasEntity() {
        return this.getEntity().isPresent();
    }

    public boolean existsEntityDataFromType(EntityType<?> entityType) {
        for (final EntityData recipe : RecipesHelper.getEntitiesRecipesList()) {
            if (recipe != null) {
                if (recipe.getEntityType() == null) {
                    continue;
                }
                if (recipe.getEntityType().equals(entityType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Entity getCachedEntity(Level level) {
        if (this.cachedEntity == null && this.entityType != null) {
            if (this.renderedEntity == null) {
                CompoundTag nbt = new CompoundTag();
                nbt.putString("id", EntityType.getKey(this.entityType).toString());
                this.renderedEntity = new SpawnData(nbt, Optional.empty(), Optional.empty());
            }
            this.cachedEntity = EntityType.loadEntityRecursive(this.renderedEntity.getEntityToSpawn(), level, EntitySpawnReason.LOAD, entity -> entity);
            CagedMobsReborn.LOGGER.debug("[CAGE-RENDER] Created cached display entity {} at {} (success={})",
                    EntityType.getKey(this.entityType), this.worldPosition, this.cachedEntity != null);
        }
        return this.cachedEntity;
    }

    private static EntityData getMobDataFromType(EntityType<?> type) {
        if (type == null) {
            return null;
        }
        EntityData finalEntityData = null;
        for (final Recipe<?> recipe : RecipesHelper.getEntitiesRecipesList()) {
            if (recipe instanceof EntityData entityData) {
                if (entityData.getEntityType() != null && entityData.getEntityType().equals(type)) {
                    finalEntityData = entityData;
                    break;
                }
            }
        }
        if (finalEntityData != null) {
            addAdditionalLootData(finalEntityData);
        }
        return finalEntityData;
    }

    private static void addAdditionalLootData(EntityData entityData) {
        for (final Recipe<?> recipe : RecipesHelper.getAdditionalLootRecipesList()) {
            if (recipe instanceof AdditionalLootData additionalLootData) {
                if (entityData.getEntityType() != null) {
                    if (entityData.getEntityType().equals(additionalLootData.getEntityType())) {
                        for (LootData data : additionalLootData.getResults()) {
                            if (!additionalLootData.isRemoveFromEntity()) {
                                if (!entityData.getResults().contains(data)) {
                                    entityData.getResults().add(data);
                                }
                            } else {
                                entityData.getResults().removeIf(lootData -> SerializationHelper.ingredientStack(lootData.getItem()).getItem().equals(SerializationHelper.ingredientStack(data.getItem()).getItem()));
                            }
                        }
                    }
                }
            }
        }
    }

    // 升级相关逻辑

    public void addUpgrade(ItemStack heldItem) {
        if (heldItem.isEmpty()) {
            return;
        }
        ItemResource resource = ItemResource.of(heldItem);
        for (int slot = ENVIRONMENT_SLOT + 1; slot < this.items.size(); slot++) {
            if (this.items.getAmountAsInt(slot) == 0 && this.items.isValid(slot, resource)) {
                this.items.set(slot, resource, 1);
                return;
            }
        }
    }

    public boolean acceptsUpgrades() {
        return this.items.getAmountAsInt(ENVIRONMENT_SLOT + 1) == 0 ||
                this.items.getAmountAsInt(ENVIRONMENT_SLOT + 2) == 0 ||
                this.items.getAmountAsInt(ENVIRONMENT_SLOT + 3) == 0;
    }

    public boolean hasAnyUpgrades() {
        return this.items.getAmountAsInt(ENVIRONMENT_SLOT + 1) > 0 ||
                this.items.getAmountAsInt(ENVIRONMENT_SLOT + 2) > 0 ||
                this.items.getAmountAsInt(ENVIRONMENT_SLOT + 3) > 0;
    }

    public int getUpgradeCount(Item upgradeItem) {
        int currentCount = 0;
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.getAmountAsInt(i) > 0 && this.items.getResource(i).is(upgradeItem)) {
                currentCount++;
            }
        }
        return currentCount;
    }

    public boolean hasUpgrades(Item upgradeItem, int requiredCount) {
        return this.getUpgradeCount(upgradeItem) >= requiredCount;
    }

    public List<ItemStack> getUpgradesAsItemStacks() {
        List<ItemStack> upgrades = new ArrayList<>();
        for (int slot = ENVIRONMENT_SLOT + 1; slot <= UPGRADES_COUNT; slot++) {
            upgrades.add(this.getStackInSlot(slot));
        }
        return upgrades;
    }

    private void emitUpgradeParticles(ItemStack upgrade, MobCageBlockEntity blockEntity) {
        if (upgrade.getItem().equals(CagedItems.COOKING_UPGRADE.get())) {
            emitCookingParticles(blockEntity);
        } else if (upgrade.getItem().equals(CagedItems.LIGHTNING_UPGRADE.get())) {
            emitLightningParticles(blockEntity);
        } else if (upgrade.getItem().equals(CagedItems.ARROW_UPGRADE.get())) {
            emitArrowParticles(blockEntity);
        } else if (upgrade.getItem().equals(CagedItems.EXPERIENCE_UPGRADE.get())) {
            emitExperienceParticles(blockEntity);
        } else if (upgrade.getItem().equals(CagedItems.LOOTING_UPGRADE.get())) {
            emitFortuneParticles(blockEntity);
        } else if (upgrade.getItem() instanceof SpeedIUpgradeItem ||
                upgrade.getItem() instanceof SpeedIIUpgradeItem ||
                upgrade.getItem() instanceof SpeedIIIUpgradeItem) {
            emitSpeedParticles(blockEntity);
        } else if (upgrade.getItem().equals(CagedItems.CREATIVE_UPGRADE.get())) {
            emitCreativeParticles(blockEntity);
        }
    }

    // 收获与战利品逻辑

    public boolean isWaitingForHarvest() {
        return this.waitingForHarvest;
    }

    public void onPlayerHarvest(BlockState state) {
        if ((!state.getValue(HOPPING) || CagedMobsReborn.SERVER_CONFIG.ifHoppingCagesDisabled()) && canPlayerHarvest()) {
            this.currentGrowTicks = 0;
            this.waitingForHarvest = false;
            List<ItemStack> drops = createDropsList();
            for (ItemStack item : drops) {
                dropItem(item.copy());
            }
            this.setChanged();
            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    private void attemptHarvest(BlockState state) {
        if (state.getValue(HOPPING) && !CagedMobsReborn.SERVER_CONFIG.ifHoppingCagesDisabled()) {
            if (this.autoHarvest()) {
                this.currentGrowTicks = 0;
            } else {
                this.currentGrowTicks = this.totalGrowTicks;
            }
        } else {
            waitingForHarvest = true;
            this.currentGrowTicks = this.totalGrowTicks;
        }
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
        this.setChanged();
    }

    private boolean autoHarvest() {
        final ResourceHandler<ItemResource> inventory = getInv(this.level, this.worldPosition.below(), Direction.UP);
        if (inventory != EmptyResourceHandler.<ItemResource>instance() && !this.level.isClientSide()) {
            NonNullList<ItemStack> drops = this.createDropsList();
            for (final ItemStack item : drops) {
                if (item.isEmpty()) {
                    continue;
                }
                ItemResource resource = ItemResource.of(item);
                try (Transaction tx = Transaction.openRoot()) {
                    int inserted = inventory.insert(resource, item.getCount(), tx);
                    if (inserted > 0) {
                        tx.commit();
                    }
                }
            }
            return true;
        }
        return false;
    }

    private ResourceHandler<ItemResource> getInv(Level world, BlockPos pos, Direction side) {
        final BlockEntity te = world.getBlockEntity(pos);
        if (te != null) {
            final ResourceHandler<ItemResource> invCap = Capabilities.Item.BLOCK.getCapability(world, pos, world.getBlockState(pos), te, side);
            if (invCap != null) {
                return invCap;
            }
        }
        if (te instanceof WorldlyContainer inv) {
            return new WorldlyContainerWrapper(inv, side);
        }
        final BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof WorldlyContainerHolder invProvider) {
            final WorldlyContainer inv = invProvider.getContainer(state, world, pos);
            return new WorldlyContainerWrapper(inv, side);
        }
        return EmptyResourceHandler.instance();
    }

    private NonNullList<ItemStack> createDropsList() {
        NonNullList<ItemStack> drops = NonNullList.create();
        List<Item> blacklistedItems = RecipesHelper.getItemsFromConfigList();
        if (this.entity.isAutoGenerated() && this.level instanceof ServerLevel serverLevel) {
            for (ItemStack stack : AutoEntityDataHelper.rollLoot(serverLevel, this.worldPosition, this.getLootEntity(serverLevel), this.getUpgradeCount(CagedItems.LOOTING_UPGRADE.get()), this.hasUpgrades(CagedItems.COOKING_UPGRADE.get(), 1))) {
                if (stack.isEmpty() || blacklistedItems.contains(stack.getItem()) != CagedMobsReborn.SERVER_CONFIG.isItemsListInWhitelistMode()) {
                    continue;
                }
                drops.add(stack);
            }
        }
        HolderLookup.Provider registries = this.level != null ? this.level.registryAccess() : null;
        for (LootData loot : this.entity.getResults()) {
            if (!CagedMobsReborn.SERVER_CONFIG.isItemsListInWhitelistMode()) {
                if (blacklistedItems.contains(SerializationHelper.ingredientStack(loot.getItem()).getItem())) {
                    continue;
                }
            } else {
                if (!blacklistedItems.contains(SerializationHelper.ingredientStack(loot.getItem()).getItem())) {
                    continue;
                }
            }
            if (loot.getColor() != -1 && loot.getColor() != this.color) {
                continue;
            }
            if (!this.hasUpgrades(CagedItems.LIGHTNING_UPGRADE.get(), 1) && loot.isLighting()) {
                continue;
            }
            if (!this.hasUpgrades(CagedItems.ARROW_UPGRADE.get(), 1) && loot.isArrow()) {
                continue;
            }
            if (this.level != null && !this.level.isClientSide() && this.level.getRandom().nextFloat() <= loot.getChance()) {
                int range = loot.getMaxAmount() - loot.getMinAmount() + 1;
                int amount = this.level.getRandom().nextInt(range) + loot.getMinAmount();
                if (amount > 0) {
                    ItemStack stack = SerializationHelper.ingredientStack(loot.getItem()).copy();
                    if (this.hasUpgrades(CagedItems.COOKING_UPGRADE.get(), 3) && loot.isCooking()) {
                        stack = new ItemStack(Items.COAL);
                    } else if (this.hasUpgrades(CagedItems.COOKING_UPGRADE.get(), 1) && loot.isCooking()) {
                        stack = SerializationHelper.ingredientStack(loot.getCookedItem()).copy();
                    } else {
                        // 应用配方中配置的药水、山羊角乐器或自定义数据组件
                        stack = loot.applyLootData(stack, registries);
                    }
                    stack.setCount(amount);
                    if (loot.ifRandomDurability()) {
                        stack = applyRandomDurability(stack);
                    }
                    drops.add(stack);
                    if (this.hasUpgrades(CagedItems.LOOTING_UPGRADE.get(), 1)) {
                        this.calculateFortune(loot, drops, stack);
                    }
                }
            }
        }
        if (this.hasUpgrades(CagedItems.EXPERIENCE_UPGRADE.get(), 1)) {
            if (this.level != null && !this.level.isClientSide() && this.level.getRandom().nextFloat() <= 0.7) {
                ItemStack experienceOrbItem = new ItemStack(CagedItems.CRYSTALLIZED_EXPERIENCE.get());
                experienceOrbItem.setCount(this.getUpgradeCount(CagedItems.EXPERIENCE_UPGRADE.get()));
                drops.add(experienceOrbItem);
                if (this.hasUpgrades(CagedItems.LOOTING_UPGRADE.get(), 1)) {
                    this.calculateFortune(null, drops, experienceOrbItem);
                }
            }
        }
        return drops;
    }

    private Entity getLootEntity(ServerLevel serverLevel) {
        if ((this.lootEntity == null || this.lootEntity.getType() != this.entityType) && this.entityType != null) {
            this.lootEntity = this.entityType.create(serverLevel, EntitySpawnReason.LOAD);
            if (this.lootEntity != null) {
                Vec3 center = Vec3.atCenterOf(this.worldPosition);
                this.lootEntity.setPos(center.x, center.y, center.z);
            }
            CagedMobsReborn.LOGGER.debug("[AUTO-ENTITY] Created virtual loot entity {} at {} (success={})",
                    EntityType.getKey(this.entityType), this.worldPosition, this.lootEntity != null);
        }
        if (this.lootEntity instanceof Sheep sheep) {
            sheep.setColor(DyeColor.byId(this.color));
        }
        return this.lootEntity;
    }

    private void calculateFortune(@Nullable LootData lootData, NonNullList<ItemStack> dropList, ItemStack item) {
        double fortuneChance = this.getUpgradeCount(CagedItems.LOOTING_UPGRADE.get()) * 0.2;
        if (this.level != null && !this.level.isClientSide() && this.level.getRandom().nextFloat() < fortuneChance) {
            int countMultiplayer = this.level.getRandom().nextInt(2) + 2;
            for (int i = 0; i < countMultiplayer - 1; i++) {
                if (lootData != null && lootData.ifRandomDurability()) {
                    item = applyRandomDurability(item.copy());
                }
                dropList.add(item);
            }
        }
    }

    public ItemStack applyRandomDurability(ItemStack stack) {
        if (this.level != null && !this.level.isClientSide() && stack.getMaxDamage() > 0) {
            int randomDurability = Math.max(1, this.level.getRandom().nextInt(stack.getMaxDamage()));
            stack.setDamageValue(randomDurability);
        }
        return stack;
    }

    private boolean canPlayerHarvest() {
        return this.hasEnvAndEntity() && this.totalGrowTicks > 0 && this.currentGrowTicks >= this.totalGrowTicks;
    }

    private boolean hasEnvAndEntity() {
        return this.hasEntity() && this.hasEnvironment();
    }

    public float getGrowthPercentage() {
        if (this.totalGrowTicks != 0) {
            if (this.currentGrowTicks >= this.totalGrowTicks) {
                return 1.0F;
            } else {
                return (float) this.currentGrowTicks / this.totalGrowTicks;
            }
        } else {
            return 0;
        }
    }

    private int calculateTotalGrowTicks() {
        if (this.environmentData != null && this.getEntity().isPresent()) {
            if (this.hasUpgrades(CagedItems.CREATIVE_UPGRADE.get(), 1)) {
                this.totalGrowTicks = 1;
                if (this.currentGrowTicks > this.totalGrowTicks) {
                    this.currentGrowTicks = this.totalGrowTicks;
                }
                return this.totalGrowTicks;
            }
            float growModifier = this.environmentData.getGrowModifier();
            for (int i = 0; i < this.getUpgradeCount(CagedItems.SPEED_I_UPGRADE.get()); i++) {
                growModifier *= 1.5F;
            }
            for (int i = 0; i < this.getUpgradeCount(CagedItems.SPEED_II_UPGRADE.get()); i++) {
                growModifier *= 2F;
            }
            for (int i = 0; i < this.getUpgradeCount(CagedItems.SPEED_III_UPGRADE.get()); i++) {
                growModifier *= 3F;
            }
            int basicTotalGrowTicks = Math.round(this.getEntity().get().getTotalGrowTicks() / growModifier);
            // 确保总生长刻数至少为 1，避免高倍率配置下四舍五入为 0 导致无法收获
            this.totalGrowTicks = Math.max(1, (int) Math.round(basicTotalGrowTicks / CagedMobsReborn.SERVER_CONFIG.getSpeedOfCages()));
            if (this.currentGrowTicks >= this.totalGrowTicks) {
                this.currentGrowTicks = this.totalGrowTicks;
            } else {
                this.waitingForHarvest = false;
            }
            return this.totalGrowTicks;
        }
        return 0;
    }

    public int getColor() {
        return this.color;
    }

    // 保存和加载方块实体状态

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        this.saveTag(output);
    }

    private void saveTag(net.minecraft.world.level.storage.ValueOutput output) {
        items.serialize(output.child(ITEMS_TAG));
        if (this.hasEntity()) {
            output.putString("entity", EntityType.getKey(this.entityType).toString());
        }
        output.putInt("color", this.color);
        output.putInt("currentGrowTicks", this.currentGrowTicks);
        output.putBoolean("waitingForHarvest", this.waitingForHarvest);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        this.loadTag(input);
    }

    private void loadTag(net.minecraft.world.level.storage.ValueInput input) {
        ItemStack oldEnv = this.getStackInSlot(ENVIRONMENT_SLOT);
        EntityType<?> oldEntityType = this.entityType;
        input.child(ITEMS_TAG).ifPresent(items::deserialize);
        this.environmentData = MobCageBlockEntity.getEnvironmentDataFromItemStack(this.getStackInSlot(ENVIRONMENT_SLOT));
        this.entityType = input.getString("entity").flatMap(EntityType::byString).orElse(null);
        this.entity = MobCageBlockEntity.getMobDataFromType(this.entityType);
        // 生物类型变更或清空时同步失效展示与战利品缓存实体
        if (!Objects.equals(oldEntityType, this.entityType)) {
            this.renderedEntity = null;
            this.cachedEntity = null;
            this.lootEntity = null;
        }
        this.color = input.getIntOr("color", 0);
        this.waitingForHarvest = input.getBooleanOr("waitingForHarvest", false);
        this.currentGrowTicks = input.getIntOr("currentGrowTicks", 0);
        if (hasEntity()) {
            this.totalGrowTicks = this.calculateTotalGrowTicks();
        }
        if (!ItemStack.matches(oldEnv, this.getStackInSlot(ENVIRONMENT_SLOT)) || !Objects.equals(oldEntityType, this.entityType)) {
            requestModelDataUpdate();
            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // 区块加载与方块更新时向客户端同步完整的笼内环境、生物与生长进度
        return this.saveCustomOnly(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
