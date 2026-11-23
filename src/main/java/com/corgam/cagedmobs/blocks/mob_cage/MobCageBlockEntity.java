package com.corgam.cagedmobsreborn.blocks.mob_cage;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.helpers.AdaptedItemHandler;
import com.corgam.cagedmobsreborn.items.upgrades.SpeedIIIUpgradeItem;
import com.corgam.cagedmobsreborn.items.upgrades.SpeedIIUpgradeItem;
import com.corgam.cagedmobsreborn.items.upgrades.SpeedIUpgradeItem;
import com.corgam.cagedmobsreborn.registers.CagedBlockEntities;
import com.corgam.cagedmobsreborn.registers.CagedItems;
import com.corgam.cagedmobsreborn.serializers.AutoEntityDataHelper;
import com.corgam.cagedmobsreborn.serializers.RecipesHelper;
import com.corgam.cagedmobsreborn.serializers.SerializationHelper;
import com.corgam.cagedmobsreborn.serializers.entity.EntityData;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentData;
import com.corgam.cagedmobsreborn.serializers.entity.AdditionalLootData;
import com.corgam.cagedmobsreborn.serializers.entity.LootData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageBlock.HOPPING;
import static com.corgam.cagedmobsreborn.helpers.UpgradeItemsParticles.*;

public class MobCageBlockEntity extends BlockEntity {

    // Entity and Environment data
    private EnvironmentData environmentData = null;
    private EntityData entity = null;
    private EntityType<?> entityType = null;
    private Entity cachedEntity;
    private SpawnData renderedEntity;
    private Entity lootEntity;
    // Ticks
    private int currentGrowTicks = 0;
    private int totalGrowTicks = 0;
    private boolean waitingForHarvest = false;
    // Color of entity
    private int color = 0;
    // Saving and loading
    public static final String ITEMS_TAG = "Inventory";
    // Item capability
    public static int UPGRADES_COUNT = 3;
    public static int ENVIRONMENT_SLOT = 0;
    public static int SLOT_COUNT = UPGRADES_COUNT + 1;
    // 物品处理器与防重复掉落标记
    private boolean droppingInventory = false;
    private final ItemStackHandler items = createItemHandler();
    private static final IItemHandler EMPTY_HANDLER = new ItemStackHandler(0);
    private final IItemHandler restrictedItemHandler = new AdaptedItemHandler(items){
        /**
         * Does not allow item extraction (for example hoppers).
         * @param slot     Slot to extract from.
         * @param amount   Amount to extract
         * @param simulate If true, the extraction is only simulated
         * @return empty item stack
         */
        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        /**
         * Does not allow item insertion (for example hoppers).
         * @param slot     Slot to insert into.
         * @param stack    ItemStack to insert. This must not be modified by the item handler.
         * @param simulate If true, the insertion is only simulated
         * @return the same input stack
         */
        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }
    };

    /**
     * Creates a new cage block entity
     * @param pPos the position of the entity
     * @param pBlockState the state of the entity
     */
    public MobCageBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(CagedBlockEntities.MOB_CAGE_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    /**
     * Creates the item handler.
     * @return the item handler.
     */
    @Nonnull
    private ItemStackHandler createItemHandler(){
        return new ItemStackHandler(SLOT_COUNT){
            @Override
            protected void onContentsChanged(int slot) {
                if (droppingInventory) {
                    return;
                }
                // 更新环境或生长周期
                if (slot == ENVIRONMENT_SLOT) {
                    updateEnvironment();
                } else {
                    calculateTotalGrowTicks();
                }
                setChanged();
                if (level != null) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
                }
            }
        };
    }

    /**
     * Returns the item handler for environment and upgrades
     * @return items handler
     */
    public ItemStackHandler getInventoryHandler(){
        return this.items;
    }

    public IItemHandler getRestrictedItemHandler() {
        return this.restrictedItemHandler;
    }

    /**
     * Main method executed in every tick. Runs only on the server instance.
     * @param level the world in which the block entity is located
     * @param pos the position of the block entity
     * @param state the state of the block entity
     * @param blockEntity the block entity class
     */
    public static void tick(Level level, BlockPos pos, BlockState state, MobCageBlockEntity blockEntity) {
        //Tick only when env and mob is inside
        if(blockEntity.hasEnvAndEntity() && !blockEntity.waitingForHarvest) {
            // Check if ready to harvest
            if(blockEntity.currentGrowTicks >= blockEntity.totalGrowTicks) {
                blockEntity.attemptHarvest(state);
            }else {
                // Add one tick (if entity requires water-logging check for it)
                if(!blockEntity.entity.ifRequiresWater() || blockEntity.getBlockState().getValue(BlockStateProperties.WATERLOGGED)){
                    blockEntity.currentGrowTicks++;
                }
            }
        }
        // Check if the cage has cooking upgrade and spawn particles
        if(level != null && level.isClientSide()){
            if(CagedMobsReborn.CLIENT_CONFIG.shouldUpgradesParticles()){
                for(ItemStack upgrade : blockEntity.getUpgradesAsItemStacks()){
                    blockEntity.emitUpgradeParticles(upgrade, blockEntity);
                }
            }
        }
    }

    /**
     * Drops the whole inventory on the ground.
     */
    public void dropInventory() {
        this.droppingInventory = true;
        try {
            for (int i = 0; i < this.items.getSlots(); i++) {
                ItemStack stack = this.items.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    this.dropItem(stack.copy());
                    this.items.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        } finally {
            this.droppingInventory = false;
        }
    }

    private void dropItem(ItemStack item) {
        if (this.level != null && !this.level.isClientSide() && !item.isEmpty()) {
            final double offsetX = (double) (level.random.nextFloat() * 0.7F) + (double) 0.15F;
            final double offsetY = (double) (level.random.nextFloat() * 0.7F) + (double) 0.060000002F + 0.6D;
            final double offsetZ = (double) (level.random.nextFloat() * 0.7F) + (double) 0.15F;
            final ItemEntity itemEntity = new ItemEntity(this.level, this.worldPosition.getX() + offsetX, this.worldPosition.getY() + offsetY, this.worldPosition.getZ() + offsetZ, item);
            itemEntity.setDefaultPickUpDelay();
            this.level.addFreshEntity(itemEntity);
        }
    }

    // ENVIRONMENT FUNCTIONS

    /**
     * Sets the environment data from held item.
     * @param heldItem the item to set the environment from
     */
    public void setEnvironment(ItemStack heldItem) {
        this.environmentData = getEnvironmentDataFromItemStack(heldItem);
        if(this.items.getStackInSlot(ENVIRONMENT_SLOT).isEmpty()){
            // Set the env item
            ItemStack itemstack = heldItem.copy();
            itemstack.setCount(1);
            this.items.insertItem(ENVIRONMENT_SLOT, itemstack, false);
        }
        // Check this block as a block to be saved upon exiting the chunk
        this.setChanged();
        // Sync with client
        if(this.level != null){
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Returns the environment data from item stack
     * @param heldItem the item stack
     * @return the environment data
     */
    public static EnvironmentData getEnvironmentDataFromItemStack(ItemStack heldItem) {
        EnvironmentData finalEnvData = null;
        for(final EnvironmentData recipe : RecipesHelper.getEnvironmentRecipesList()) {
            if(recipe != null) {
                if(recipe.getInputItem().test(heldItem)) {
                    finalEnvData = recipe;
                    break;
                }
            }
        }
        return finalEnvData;
    }

    /**
     * Removes the environment data.
     */
    public void removeEnvironment(){
        this.environmentData = null;
        // Update entity
        this.removeEntity();
        // Check this block as a block to be saved upon exiting the chunk
        this.setChanged();
        // Sync with client
        if(this.level != null){
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Updates the environment from currently held item.
     */
    public void updateEnvironment() {
        ItemStack envItem = this.items.getStackInSlot(ENVIRONMENT_SLOT);
        if (envItem.isEmpty()) {
            this.removeEnvironment();
        } else {
            envItem.setCount(1);
            if (this.environmentData == null || !this.environmentData.getInputItem().test(envItem)) {
                this.setEnvironment(envItem);
            }
        }
    }

    public EnvironmentData getEnvironmentData() {
        // 客户端区块更新早于配方同步到达时延迟补解析环境配方
        if (this.environmentData == null && this.hasEnvironment()) {
            this.environmentData = getEnvironmentDataFromItemStack(this.items.getStackInSlot(ENVIRONMENT_SLOT));
        }
        return this.environmentData;
    }

    /**
     * Returns the current environment item stack
     * @return environment item stack
     */
    public ItemStack getEnvironmentItemStack(){
        return this.items.getStackInSlot(ENVIRONMENT_SLOT);
    }

    /**
     * Checks if the block entity has environment data.
     * @return if block entity has environment data.
     */
    public boolean hasEnvironment() {
        return !this.items.getStackInSlot(ENVIRONMENT_SLOT).isEmpty();
    }

    /**
     * Check if there exists an environment data from the given item.
     * @param heldItem item to check
     * @return if environment data exists
     */
    public static boolean existsEnvironmentFromItemStack(ItemStack heldItem) {
        // Check if the hand is empty
        if(heldItem.isEmpty()){
            return false;
        }
        // Check the recipes
        for(final Recipe<?> recipe : RecipesHelper.getEnvironmentRecipesList()) {
            if(recipe instanceof EnvironmentData envData) {
                if(envData.getInputItem().test(heldItem)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check the current environment is suitable for given entity type.
     * @param player player checking
     * @param entityType entity type to check
     * @param state block state
     * @return if environment is suitable
     */
    public boolean isEnvironmentSuitable(Player player, EntityType<?> entityType, BlockState state) {
        EntityData recipe = getMobDataFromType(entityType);
        if (recipe == null) {
            return false;
        }
        // Check if entity needs waterlogged cage
        if(recipe.ifRequiresWater() && !state.getValue(BlockStateProperties.WATERLOGGED)){
            player.displayClientMessage(Component.translatable("block.cagedmobsreborn.mob_cage.requiresWater").withStyle(ChatFormatting.RED), true);
            return false;
        }
        boolean universal = false;
        if(this.environmentData != null){
            universal = this.environmentData.getCategories().contains(RecipesHelper.UNIVERSAL_ENVIRONMENT);
            if(universal && RecipesHelper.isUniversalEnvironmentAllowed(entityType)){
                return true;
            }
            for(String env : this.environmentData.getCategories()){
                if(recipe.getEnvironments().contains(env)){
                    return true;
                }
            }
        }
        if(universal){
            player.displayClientMessage(Component.translatable("block.cagedmobsreborn.mob_cage.universalBlacklisted").withStyle(ChatFormatting.RED), true);
            return false;
        }
        player.displayClientMessage(Component.translatable("block.cagedmobsreborn.mob_cage.envNotSuitable").withStyle(ChatFormatting.RED), true);
        return false;
    }

    // ENTITY FUNCTIONS

    /**
     * Sets the environment data from sampler item.
     * @param entityType entity type
     * @param sampler sampler item
     */
    public void setEntityFromSampler(EntityType<?> entityType, ItemStack sampler) {
        this.color = 0;
        if (entityType.toString().contains("sheep")) {
            if (SerializationHelper.hasCustomData(sampler) && SerializationHelper.getCustomData(sampler).contains("Color")) {
                this.color = SerializationHelper.getCustomData(sampler).getInt("Color");
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
        // 客户端区块更新早于配方同步到达时延迟补解析生物配方
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

    /**
     * Checks if there exists a mob data from entity type
     * @param entityType the entity type to check
     * @return if there exists mob data
     */
    public boolean existsEntityDataFromType(EntityType<?> entityType) {
        for(final EntityData recipe : RecipesHelper.getEntitiesRecipesList()) {
            if(recipe != null) {
                // Check for null exception
                if(recipe.getEntityType() == null){continue;}
                if(recipe.getEntityType().equals(entityType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the cached entity.
     * @param level the world of the entity
     * @return the entity
     */
    public Entity getCachedEntity(Level level) {
        if (this.cachedEntity == null && this.entityType != null) {
            if (this.renderedEntity == null) {
                CompoundTag nbt = new CompoundTag();
                nbt.putString("id", EntityType.getKey(this.entityType).toString());
                this.renderedEntity = new SpawnData(nbt, Optional.empty(), Optional.empty());
            }
            this.cachedEntity = EntityType.loadEntityRecursive(this.renderedEntity.getEntityToSpawn(), level, Function.identity());
        }
        return this.cachedEntity;
    }

    private static EntityData getMobDataFromType(EntityType<?> type) {
        if (type == null) {
            return null;
        }
        EntityData finalEntityData = null;
        // Get the mobData
        for(final Recipe<?> recipe : RecipesHelper.getEntitiesRecipesList()) {
            if(recipe instanceof EntityData entityData) {
                // Check for null exception
                if(entityData.getEntityType() != null && entityData.getEntityType().equals(type)){
                    finalEntityData = entityData;
                    break;
                }
            }
        }
        // Add additional Loot
        if(finalEntityData != null){
            addAdditionalLootData(finalEntityData);
        }
        return finalEntityData;
    }

    /**
     * Adds all loot to the additional loot data object
     * @param entityData the object to add items to
     */
    private static void addAdditionalLootData(EntityData entityData){
        for(final Recipe<?> recipe : RecipesHelper.getAdditionalLootRecipesList()) {
            if(recipe instanceof AdditionalLootData additionalLootData) {
                // Check for null exception
                if(entityData.getEntityType() != null){
                    // If entity types are equal
                    if(entityData.getEntityType().equals(additionalLootData.getEntityType())) {
                        for(LootData data : additionalLootData.getResults()){
                            // Add loot
                            if(!additionalLootData.isRemoveFromEntity()){
                                if(!entityData.getResults().contains(data)){
                                    entityData.getResults().add(data);
                                }
                            // Remove loot
                            }else{
                                entityData.getResults().removeIf(lootData -> lootData.getItem().getItems()[0].getItem().equals(data.getItem().getItems()[0].getItem()));
                            }
                        }
                    }
                }
            }
        }
    }

    // UPGRADES FUNCTIONS

    /**
     * Adds a new upgrade to the cage
     * @param heldItem upgrade to add.
     */
    public void addUpgrade(ItemStack heldItem) {
        for (int slot = ENVIRONMENT_SLOT+1; slot < this.items.getSlots(); slot++) {
            // Check for empty slot
            if(this.items.getStackInSlot(slot).isEmpty()){
                ItemStack upgrade = heldItem.copy();
                upgrade.setCount(1);
                // Check if upgrade is valid
                if(this.items.isItemValid(slot, upgrade)){
                    // Insert upgrade
                    this.items.insertItem(slot, upgrade, false);
                    return;
                }
            }
        }
    }

    /**
     * Returns if the cage can accept more upgrades.
     * @return if cage can accept more upgrades.
     */
    public boolean acceptsUpgrades() {
        return this.items.getStackInSlot(ENVIRONMENT_SLOT+1).isEmpty() ||
                this.items.getStackInSlot(ENVIRONMENT_SLOT+2).isEmpty() ||
                this.items.getStackInSlot(ENVIRONMENT_SLOT+3).isEmpty();
    }

    /**
     * Returns if the cage has any upgrade.
     * @return if the cage has any upgrade
     */
    public boolean hasAnyUpgrades(){
        return !this.items.getStackInSlot(ENVIRONMENT_SLOT+1).isEmpty() ||
                !this.items.getStackInSlot(ENVIRONMENT_SLOT+2).isEmpty() ||
                !this.items.getStackInSlot(ENVIRONMENT_SLOT+3).isEmpty();
    }

    /**
     * Return the amount of a specific upgrade.
     * @param upgradeItem the upgrade to check for
     * @return the amount of specific upgrade
     */
    public int getUpgradeCount(Item upgradeItem){
        int currentCount = 0;
        for (int i = 0; i < this.items.getSlots(); i++) {
            if(this.items.getStackInSlot(i).getItem().equals(upgradeItem)){
                currentCount++;
            }
        }
        return currentCount;
    }

    public boolean hasUpgrades(Item upgradeItem, int requiredCount){
        int currentCount = 0;
        for (int i = 0; i < this.items.getSlots(); i++) {
            if(this.items.getStackInSlot(i).getItem().equals(upgradeItem)){
                currentCount++;
            }
        }
        return currentCount >= requiredCount;
    }

    /**
     * Returns a list of upgrades as ItemStacks.
     * @return list of upgrades
     */
    public List<ItemStack> getUpgradesAsItemStacks(){
        List<ItemStack> upgrades = new ArrayList<>();
        for(int slot = ENVIRONMENT_SLOT+1; slot <= UPGRADES_COUNT; slot++){
            upgrades.add(this.items.getStackInSlot(slot));
        }
        return upgrades;
    }

    /**
     * Emits particles specific for the given upgrade.
     * @param upgrade upgrade item stack
     */
    private void emitUpgradeParticles(ItemStack upgrade, MobCageBlockEntity blockEntity) {
        if(upgrade.getItem().equals(CagedItems.COOKING_UPGRADE.get())){
            emitCookingParticles(blockEntity);
        }else if(upgrade.getItem().equals(CagedItems.LIGHTNING_UPGRADE.get())){
            emitLightningParticles(blockEntity);
        }else if(upgrade.getItem().equals(CagedItems.ARROW_UPGRADE.get())){
            emitArrowParticles(blockEntity);
        }else if(upgrade.getItem().equals(CagedItems.EXPERIENCE_UPGRADE.get())){
            emitExperienceParticles(blockEntity);
        }else if(upgrade.getItem().equals(CagedItems.LOOTING_UPGRADE.get())){
            emitFortuneParticles(blockEntity);
        }else if(upgrade.getItem() instanceof SpeedIUpgradeItem ||
        upgrade.getItem() instanceof SpeedIIUpgradeItem ||
        upgrade.getItem() instanceof SpeedIIIUpgradeItem){
            emitSpeedParticles(blockEntity);
        }else if(upgrade.getItem().equals(CagedItems.CREATIVE_UPGRADE.get())){
            emitCreativeParticles(blockEntity);
        }
    }
    // HARVESTING FUNCTIONS

    /**
     * Returns if the cage is waiting for harvest
     * @return if waiting for harvest
     */
    public boolean isWaitingForHarvest() {
        return this.waitingForHarvest;
    }

    /**
     * Called when player is harvesting the cage.
     */
    public void onPlayerHarvest(BlockState state) {
        if((!state.getValue(HOPPING)|| CagedMobsReborn.SERVER_CONFIG.ifHoppingCagesDisabled()) && canPlayerHarvest()){
            this.currentGrowTicks = 0;
            this.waitingForHarvest = false;
            List<ItemStack> drops = createDropsList();
            for( ItemStack item : drops) {
                dropItem(item.copy());
            }
            this.setChanged();
            // Sync with client
            if(this.level != null){
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
            }
            // Check this block as a block to be saved upon exiting the chunk
            this.setChanged();
        }
    }

    /**
     * Attempt harvest.
     * For hopping cages and an inventory bellow harvest.
     * For not hopping cages, lock and wait for players interaction.
     * @param state the block state
     */
    private void attemptHarvest(BlockState state) {
        if(state.getValue(HOPPING) && !CagedMobsReborn.SERVER_CONFIG.ifHoppingCagesDisabled()) {
            // Try to auto harvest
            if(this.autoHarvest()){
                this.currentGrowTicks = 0;
            }else{
                this.currentGrowTicks = this.totalGrowTicks;
            }
        }else {
            // Lock
            waitingForHarvest = true;
            this.currentGrowTicks = this.totalGrowTicks;
        }
        // Sync with client
        if(this.level != null){
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
        // Check this block as a block to be saved upon exiting the chunk
        this.setChanged();
    }

    /**
     * Auto-harvests the cage, when there is a valid inventory bellow.
     * @return if the harvest happened
     */
    private boolean autoHarvest() {
        final IItemHandler inventory = getInv(this.level, this.worldPosition.below(), Direction.UP);
        if(inventory != EMPTY_HANDLER && !this.level.isClientSide()){
            // For every item in drop list
            NonNullList<ItemStack> drops =this.createDropsList();
            for(final ItemStack item : drops){
                // For every slot in inv
                for(int slot = 0; slot < inventory.getSlots(); slot++){
                    // Simulate the insert
                    if(inventory.isItemValid(slot, item) && inventory.insertItem(slot,item,true).getCount() != item.getCount()){
                        // Actual insert
                        inventory.insertItem(slot, item, false);
                        break;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the ItemHandler from the block
     * @param world the world of the block
     * @param pos the position of the block
     * @param side the side of the block
     * @return the ItemHandler
     */
    private IItemHandler getInv(Level world, BlockPos pos, Direction side){
        final BlockEntity te = world.getBlockEntity(pos);
        // Capability system
        if(te != null){
            final IItemHandler invCap = Capabilities.ItemHandler.BLOCK.getCapability(world, pos, world.getBlockState(pos), te, side);
            return invCap == null ? EMPTY_HANDLER : invCap;
        }else{
            // When block doesn't use capability system
            final BlockState state = world.getBlockState(pos);
            if(state.getBlock() instanceof WorldlyContainerHolder invProvider){
                final WorldlyContainer inv = invProvider.getContainer(state, world, pos);
                return new SidedInvWrapper(inv, side);
            }
        }
        return EMPTY_HANDLER;
    }

    /**
     * Creates the drop list for the entity.
     * @return a list of items to drop.
     */
    private NonNullList<ItemStack> createDropsList(){
        NonNullList<ItemStack> drops = NonNullList.create();
        List<Item> blacklistedItems = RecipesHelper.getItemsFromConfigList();
        if(this.entity.isAutoGenerated() && this.level instanceof ServerLevel serverLevel){
            for(ItemStack stack : AutoEntityDataHelper.rollLoot(serverLevel, this.worldPosition, this.getLootEntity(serverLevel), this.getUpgradeCount(CagedItems.LOOTING_UPGRADE.get()), this.hasUpgrades(CagedItems.COOKING_UPGRADE.get(), 1))){
                if(stack.isEmpty() || blacklistedItems.contains(stack.getItem()) != CagedMobsReborn.SERVER_CONFIG.isItemsListInWhitelistMode()){
                    continue;
                }
                drops.add(stack);
            }
        }
        for(LootData loot : this.entity.getResults()) {
            // Skip item if it's blacklisted or whitelisted
            if(!CagedMobsReborn.SERVER_CONFIG.isItemsListInWhitelistMode()){
                if(blacklistedItems.contains(loot.getItem().getItems()[0].getItem())){
                    continue;
                }
            }else{
                if(!blacklistedItems.contains(loot.getItem().getItems()[0].getItem())){
                    continue;
                }
            }
            // Choose a loot type for entity's color
            if(loot.getColor() != -1){
                if(loot.getColor() != this.color){
                    continue;
                }
            }
            // Skip if loot needs lightning upgrade, but it's not present in the cage.
            if(!this.hasUpgrades(CagedItems.LIGHTNING_UPGRADE.get(), 1) && loot.isLighting()){
                continue;
            }
            // Skip if loot needs arrow upgrade, but it's not present in the cage.
            if(!this.hasUpgrades(CagedItems.ARROW_UPGRADE.get(), 1) && loot.isArrow()){
                continue;
            }
            if(this.level != null && !this.level.isClientSide() && this.level.random.nextFloat() <= loot.getChance()) {
                // Roll the amount of items
                int range = loot.getMaxAmount() - loot.getMinAmount() + 1;
                int amount = this.level.random.nextInt(range) + loot.getMinAmount();
                if(amount > 0) {
                    // Add copied item stack to the drop list
                    ItemStack stack = loot.getItem().getItems()[0].copy();
                    // Replace the item if there is a cooking upgrade.
                    if(this.hasUpgrades(CagedItems.COOKING_UPGRADE.get(), 3) && loot.isCooking()){
                        stack = new ItemStack(Items.COAL);
                    }else if(this.hasUpgrades(CagedItems.COOKING_UPGRADE.get(), 1) && loot.isCooking()){
                        stack = loot.getCookedItem().getItems()[0].copy();
                    }
                    stack.setCount(amount);
                    if(loot.ifRandomDurability()){
                        stack = applyRandomDurability(stack);
                    }
                    drops.add(stack);
                    if(this.hasUpgrades(CagedItems.LOOTING_UPGRADE.get(),1 )){
                        this.calculateFortune(loot, drops, stack);
                    }
                }
            }
        }
        // Add experience orb if the experience upgrade is present
        if(this.hasUpgrades(CagedItems.EXPERIENCE_UPGRADE.get(), 1)){
            if(this.level != null && !this.level.isClientSide() && this.level.random.nextFloat() <= 0.7){
                ItemStack experienceOrbItem = new ItemStack(CagedItems.CRYSTALLIZED_EXPERIENCE.get());
                experienceOrbItem.setCount(this.getUpgradeCount(CagedItems.EXPERIENCE_UPGRADE.get()));
                drops.add(experienceOrbItem);
                // Take fortune upgrade into account
               if(this.hasUpgrades(CagedItems.LOOTING_UPGRADE.get(),1 )){
                    this.calculateFortune(null, drops, experienceOrbItem);
               }
            }
        }
        return drops;
    }

    private Entity getLootEntity(ServerLevel serverLevel) {
        if ((this.lootEntity == null || this.lootEntity.getType() != this.entityType) && this.entityType != null) {
            this.lootEntity = this.entityType.create(serverLevel);
            if (this.lootEntity != null) {
                Vec3 center = Vec3.atCenterOf(this.worldPosition);
                this.lootEntity.setPos(center.x, center.y, center.z);
            }
        }
        if (this.lootEntity instanceof Sheep sheep) {
            sheep.setColor(DyeColor.byId(this.color));
        }
        return this.lootEntity;
    }

    /**
     * Takes into account the fortune upgrade, multiplying the item drop by 2,3 or 4.
     * Each fortune upgrade adds 20% chance for the fortune effect to take place.
     * @param dropList the main drop list to add items to
     * @param item item to duplicate
     */
    private void calculateFortune(@Nullable LootData lootData, NonNullList<ItemStack> dropList, ItemStack item){
        double fortuneChance = this.getUpgradeCount(CagedItems.LOOTING_UPGRADE.get()) * 0.2;
        if(this.level != null && !this.level.isClientSide() && this.level.random.nextFloat() < fortuneChance){
            int countMultiplayer = this.level.random.nextInt(2) + 2;
            for(int i = 0; i < countMultiplayer - 1; i++){
                if(lootData !=null && lootData.ifRandomDurability()){
                    item = applyRandomDurability(item.copy());
                }
                dropList.add(item);
            }
        }
    }

    /**
     * Applies random durability to the item stack
     * @param stack item stack
     */
    public ItemStack applyRandomDurability(ItemStack stack){
        if(this.level != null && !this.level.isClientSide() && stack.getMaxDamage() > 0){
            int randomDurability = Math.max(1,this.level.random.nextInt(stack.getMaxDamage()));
            stack.setDamageValue(randomDurability);
        }
        return stack;
    }

    /**
     * Check if the cage is ready to be harvested.
     * @return if the cage is ready to be harvested
     */
    private boolean canPlayerHarvest() {
        return this.hasEnvAndEntity() && this.totalGrowTicks > 0 && this.currentGrowTicks >= this.totalGrowTicks;
    }

    /**
     * Checks if the cage has both the entity and the environment.
     * @return if the cage has both the entity and the environment
     */
    private boolean hasEnvAndEntity() {
        return this.hasEntity() && this.hasEnvironment();
    }

    /**
     * Returns the current growth percentage as float between 0.0F and 1.0F,
     * where 1.0F is the fully grown entity.
     * @return the current growth percentage
     */
    public float getGrowthPercentage() {
        if(this.totalGrowTicks != 0) {
            if(this.currentGrowTicks >= this.totalGrowTicks){
                return 1.0F;
            }else{
                return (float) this.currentGrowTicks / this.totalGrowTicks;
            }
        }else{
            return 0;
        }
    }

    /**
     * Returns the total grow ticks
     * @return total grow ticks
     */
    private int calculateTotalGrowTicks() {
        if(this.environmentData != null && this.getEntity().isPresent()){
            // Take into account creative upgrade
            if(this.hasUpgrades(CagedItems.CREATIVE_UPGRADE.get(), 1)){
                this.totalGrowTicks = 1;
                // Take into account current growth ticks
                if(this.currentGrowTicks > this.totalGrowTicks){
                    this.currentGrowTicks = this.totalGrowTicks;
                }
                return this.totalGrowTicks;
            }
            float growModifier = this.environmentData.getGrowModifier();
            // Take into account speed upgrades
            for(int i=0; i < this.getUpgradeCount(CagedItems.SPEED_I_UPGRADE.get()); i++){
                growModifier *= 1.5F;
            }
            for(int i=0; i < this.getUpgradeCount(CagedItems.SPEED_II_UPGRADE.get()); i++){
                growModifier *= 2F;
            }
            for(int i=0; i < this.getUpgradeCount(CagedItems.SPEED_III_UPGRADE.get()); i++){
                growModifier *= 3F;
            }
            // Get grow ticks from the entity
            int basicTotalGrowTicks = Math.round(this.getEntity().get().getTotalGrowTicks()/growModifier);
            // 确保总生长刻数至少为 1，避免高倍率配置下四舍五入为 0 导致无法收获
            this.totalGrowTicks = Math.max(1, (int) Math.round(basicTotalGrowTicks / CagedMobsReborn.SERVER_CONFIG.getSpeedOfCages()));
            // Round up the ticks to the max amount
            if(this.currentGrowTicks >= this.totalGrowTicks){
                this.currentGrowTicks = this.totalGrowTicks;
            }else{
                // If the cage was waiting for harvest and now it is not take that into account.
                this.waitingForHarvest = false;
            }
            return this.totalGrowTicks;
        }
        return 0;
    }

    // COLOR FUNCTIONS

    /**
     * Returns the color nbt of the entity
     * @return color id
     */
    public int getColor() {
        return this.color;
    }

    // SAVING AND LOADING

    /**
     * Saves all the additional tags of the block entity.
     * @param tag the nbt tag to save to
     */
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider provider){
        super.saveAdditional(tag, provider);
        this.saveTag(tag, provider);
    }

    private void saveTag(@NotNull CompoundTag tag, HolderLookup.Provider provider){
        // Item capability
        tag.put(ITEMS_TAG, items.serializeNBT(provider));
        // Put entity type
        if(this.hasEntity()){
            SerializationHelper.serializeEntityTypeNBT(tag, this.entityType);
        }
        // Put color
        tag.putInt("color",this.color);
        // Put ticks info
        tag.putInt("currentGrowTicks", this.currentGrowTicks);
        tag.putBoolean("waitingForHarvest", this.waitingForHarvest);
    }

    /**
     * Loads all the additional tags of the block entity.
     * @param tag the nbt tag to load from
     */
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.loadTag(tag, provider);
    }

    /**
     * Loads the block entity data from the tag.
     * @param tag the tag to load
     */
    private void loadTag(@NotNull CompoundTag tag, HolderLookup.Provider provider) {
        ItemStack oldEnv = this.items.getStackInSlot(ENVIRONMENT_SLOT).copy();
        EntityType<?> oldEntityType = this.entityType;
        if (tag.contains(ITEMS_TAG)) {
            items.deserializeNBT(provider, tag.getCompound(ITEMS_TAG));
        }
        this.environmentData = MobCageBlockEntity.getEnvironmentDataFromItemStack(this.items.getStackInSlot(ENVIRONMENT_SLOT));
        this.entityType = SerializationHelper.deserializeEntityTypeNBT(tag);
        this.entity = MobCageBlockEntity.getMobDataFromType(this.entityType);
        // 生物类型变更或清空时同步失效展示与战利品缓存实体
        if (!Objects.equals(oldEntityType, this.entityType)) {
            this.renderedEntity = null;
            this.cachedEntity = null;
            this.lootEntity = null;
        }
        this.color = tag.getInt("color");
        this.waitingForHarvest = tag.getBoolean("waitingForHarvest");
        this.currentGrowTicks = tag.getInt("currentGrowTicks");
        if (hasEntity()) {
            this.totalGrowTicks = this.calculateTotalGrowTicks();
        }
        if (!ItemStack.matches(oldEnv, this.items.getStackInSlot(ENVIRONMENT_SLOT)) || !Objects.equals(oldEntityType, this.entityType)) {
            requestModelDataUpdate();
            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    // CLIENT-SERVER SYNCHRONIZATION

    /**
     * Creates the update tag on server side, when a new chunk is loaded.
     * @return the update nbt tag to send
     */
    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        this.saveTag(tag, provider);
        return tag;
    }

    /**
     * Reads the update tag on client side, when a new chunk is loaded.
     * @param tag The {@link CompoundTag} sent from {@link BlockEntity#getUpdateTag()}
     */
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        if(tag != null){
            this.loadTag(tag, provider);
        }
    }

    /**
     * Creates an update packet when the block state has changed.
     * @return the update packet
     */
    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Loads the received data packet on client side, when the block state has changed.
     * @param net The NetworkManager the packet originated from
     * @param pkt The data packet
     */
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            handleUpdateTag(tag, provider);
        }
    }
}
