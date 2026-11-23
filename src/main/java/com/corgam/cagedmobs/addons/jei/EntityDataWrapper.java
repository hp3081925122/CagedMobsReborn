package com.corgam.cagedmobsreborn.addons.jei;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageBlockEntity;
import com.corgam.cagedmobsreborn.helpers.EntityRendererHelper;
import com.corgam.cagedmobsreborn.registers.CagedItems;
import com.corgam.cagedmobsreborn.serializers.RecipesHelper;
import com.corgam.cagedmobsreborn.serializers.SerializationHelper;
import com.corgam.cagedmobsreborn.serializers.entity.EntityData;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentData;
import com.corgam.cagedmobsreborn.serializers.entity.AdditionalLootData;
import com.corgam.cagedmobsreborn.serializers.entity.LootData;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EntityDataWrapper implements IRecipeCategoryExtension<EntityData> {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    public static final DecimalFormat SIGNED_DECIMAL_FORMAT = new DecimalFormat("+#.##;-#.##");

    private final EntityData entityData;
    private final List<ItemStack> envs = NonNullList.create();
    private final List<LootData> drops = NonNullList.create();
    private final List<ItemStack> samplers = NonNullList.create();
    private final List<Integer> cookedIDs = new ArrayList<>();
    private final boolean requiresWater;
    private final int ticks;

    public static float rotation = 0.0f;
    private static double yaw = 0;

    public EntityDataWrapper(EntityData entityData){
        this.entityData = entityData;
        // Read valid envs based on entity
        for(EnvironmentData env : RecipesHelper.getEnvironmentRecipesList()){
            if(RecipesHelper.isEnvValidForEntity(entityData,env)){
                this.envs.add(SerializationHelper.ingredientStack(env.getInputItem()));
            }
        }
        // Add DNA samplers to the recipe based on the tier of recipe
        if(entityData.getSamplerTier() >= 3){
            this.samplers.add(new ItemStack(CagedItems.NETHERITE_DNA_SAMPLER.get()));
        }else if(entityData.getSamplerTier() == 2){
            this.samplers.add(new ItemStack(CagedItems.NETHERITE_DNA_SAMPLER.get()));
            this.samplers.add(new ItemStack(CagedItems.DIAMOND_DNA_SAMPLER.get()));
        }else{
            this.samplers.add(new ItemStack(CagedItems.NETHERITE_DNA_SAMPLER.get()));
            this.samplers.add(new ItemStack(CagedItems.DIAMOND_DNA_SAMPLER.get()));
            this.samplers.add(new ItemStack(CagedItems.DNA_SAMPLER.get()));
        }
        // Add loot
        int lootIndex = 0;
        List<Item> blacklistedItems = RecipesHelper.getItemsFromConfigList();
        for(LootData data : entityData.getResults()){
            if(!CagedMobsReborn.SERVER_CONFIG.isItemsListInWhitelistMode()){
                if(!blacklistedItems.contains(SerializationHelper.ingredientStack(data.getItem()).getItem())){
                    if(!this.drops.contains(data)) {
                        this.drops.add(data);
                        lootIndex++;
                        // If it has a cooked variant add one more LootData
                        if (data.isCooking()) {
                            this.drops.add(data);
                            // If added cooked variant keep track of cooked IDs
                            this.cookedIDs.add(lootIndex);
                            lootIndex++;
                        }
                    }
                }
            }else{
                if(blacklistedItems.contains(SerializationHelper.ingredientStack(data.getItem()).getItem())){
                    if(!this.drops.contains(data)) {
                        this.drops.add(data);
                        lootIndex++;
                        // If it has a cooked variant add one more LootData
                        if (data.isCooking()) {
                            this.drops.add(data);
                            // If added cooked variant keep track of cooked IDs
                            this.cookedIDs.add(lootIndex);
                            lootIndex++;
                        }
                    }
                }
            }

        }
        // Add additional Loot
        for(final Recipe<?> recipe : RecipesHelper.getAdditionalLootRecipesList()) {
            if(recipe instanceof AdditionalLootData additionalLootData) {
                // Check for null exceptions
                if(additionalLootData.getEntityType() != null && this.entityData.getEntityType() != null){
                    // Check if the same entity type
                    if(this.entityData.getEntityType().equals(additionalLootData.getEntityType())) {
                        // For each loot data
                        for(LootData data : additionalLootData.getResults()){
                            // Add loot
                            if(!additionalLootData.isRemoveFromEntity()){
                                if(!CagedMobsReborn.SERVER_CONFIG.isItemsListInWhitelistMode()){
                                    if(!blacklistedItems.contains(SerializationHelper.ingredientStack(data.getItem()).getItem())){
                                        if(!this.drops.contains(data)){
                                            this.drops.add(data);
                                            lootIndex++;
                                            // If it has a cooked variant add one more LootData
                                            if(data.isCooking()){
                                                this.drops.add(data);
                                                // If added cooked variant keep track of cooked IDs
                                                this.cookedIDs.add(lootIndex);
                                                lootIndex++;
                                            }
                                        }
                                    }
                                }else{
                                    if(blacklistedItems.contains(SerializationHelper.ingredientStack(data.getItem()).getItem())){
                                        if(!this.drops.contains(data)){
                                            this.drops.add(data);
                                            lootIndex++;
                                            // If it has a cooked variant add one more LootData
                                            if(data.isCooking()){
                                                this.drops.add(data);
                                                // If added cooked variant keep track of cooked IDs
                                                this.cookedIDs.add(lootIndex);
                                                lootIndex++;
                                            }
                                        }
                                    }
                                }
                            // Remove loot
                            }else{
                                this.drops.removeIf(drop -> SerializationHelper.ingredientStack(drop.getItem()).getItem().equals(SerializationHelper.ingredientStack(data.getItem()).getItem()));
                            }
                        }
                    }
                }
            }
        }
        // Set up required ticks
        this.ticks = entityData.getTotalGrowTicks();
        // Set up if the recipe requires water
        this.requiresWater = entityData.ifRequiresWater();
    }

    public List<LootData> getDrops() {
        return this.drops;
    }

    public List<ItemStack> getEnvsItems() {
        return this.envs;
    }

    public EntityData getEntityData() {
        return entityData;
    }

    public List<ItemStack> getSamplers() {
        return this.samplers;
    }

    public List<Integer> getCookedIDs() {
        return this.cookedIDs;
    }

    public int getTicks(){
        return this.ticks;
    }

    public int getSeconds(){
        return this.ticks/20;
    }

    public boolean ifRequiresWater(){
        return this.requiresWater;
    }

    public void setRecipe(IRecipeLayoutBuilder builder) {
        // Add samplers without NBT for easier search
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(this.getSamplers());

        // Add DNA Samplers with the specific NBT data
        final IRecipeSlotBuilder samplersSlot = builder.addSlot(RecipeIngredientRole.INPUT, 15, 62+20);
        samplersSlot.addItemStacks(this.getSampledSamplers()).setSlotName("samplers");
        if(!CagedMobsReborn.SERVER_CONFIG.areSpawnEggsDisabled()){
            SpawnEggItem spawnEgg = SpawnEggItem.byId(entityData.getEntityType()).map(holder -> holder.value()).filter(SpawnEggItem.class::isInstance).map(SpawnEggItem.class::cast).orElse(null);
            if(spawnEgg != null){
                samplersSlot.add(spawnEgg.getDefaultInstance());
            }
        }

        // Soil Inputs
        final IRecipeSlotBuilder environmentsSlot = builder.addSlot(RecipeIngredientRole.INPUT, 15 + 20, 62+20);
        environmentsSlot.addItemStacks(this.getEnvsItems()).setSlotName("environments");
        environmentsSlot.addRichTooltipCallback(this.getEnvTooltip());

        net.minecraft.core.HolderLookup.Provider registries = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.registryAccess() : null;
        int nextSlotId = 2;
        List<Item> blacklistedItems = RecipesHelper.getItemsFromConfigList();
        for (final LootData entry : this.getDrops()) {
            boolean allowed = CagedMobsReborn.SERVER_CONFIG.isItemsListInWhitelistMode()
                    ? blacklistedItems.contains(SerializationHelper.ingredientStack(entry.getItem()).getItem())
                    : !blacklistedItems.contains(SerializationHelper.ingredientStack(entry.getItem()).getItem());
            if (allowed) {
                int relativeSlotId = nextSlotId - 2;
                final IRecipeSlotBuilder lootSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 101 + 19 * (relativeSlotId % 4), 6 + 19 * (relativeSlotId / 4));
                if (entry.isCooking() && this.getCookedIDs().contains(relativeSlotId)) {
                    lootSlot.add(SerializationHelper.ingredientStack(entry.getCookedItem()).copy());
                } else {
                    // 应用配方组件/NBT（如药水效果与山羊角乐器），确保 JEI 准确展示
                    ItemStack displayStack = entry.applyLootData(SerializationHelper.ingredientStack(entry.getItem()).copy(), registries);
                    lootSlot.add(displayStack);
                }
                nextSlotId++;
                lootSlot.addRichTooltipCallback(this.getLootTooltip(entry));
            }
        }
    }

    public void draw(GuiGraphicsExtractor pGuiGraphics, IGuiHelper guiHelper) {
        // Draw Seed & Soil
        guiHelper.getSlotDrawable().draw(pGuiGraphics, 14, 62+19);
        guiHelper.getSlotDrawable().draw(pGuiGraphics, 14+20, 62 + 19);
        // Draw Drops
        int slotCount = this.entityData.isAutoGenerated() ? Math.min(20, this.drops.size()) : 20;
        for (int nextSlotId = 2; nextSlotId < 2 + slotCount; nextSlotId++) {
            final int relativeSlotId = nextSlotId - 2;
            guiHelper.getSlotDrawable().draw(pGuiGraphics, 100 + 19 * (relativeSlotId % 4), 5 + 19 * (relativeSlotId / 4));
        }
        if(this.entityData.isAutoGenerated()){
            pGuiGraphics.textWithWordWrap(Minecraft.getInstance().font, Component.translatable("jei.tooltip.cagedmobsreborn.entity.autoLoot"), 102, 6 + 19 * ((this.drops.size() + 3) / 4) + 2, 72, 0xFF404040);
        }
        // Create the entity object to render
        Level level = Minecraft.getInstance().level;
        Optional<Entity> entity = EntityRendererHelper.createEntity(level, this.getEntityData().getEntityType(), null);
        // 渲染旋转的 3D 生物预览（定位到 JEI 背景图左侧笼子框内）
        if(entity.isPresent()){
            rotation = (rotation + 0.5f) % 360;
            EntityRendererHelper.renderEntity(pGuiGraphics, 9, 13, 57, 77, rotation, entity.get());
            yaw = (yaw + 1.5) % 720.0F;
        }
        // 绘制生物名称、生长时间与含水提示（补全不透明 Alpha 通道）
        if(this.getEntityData() != null && this.getEntityData().getEntityType() != null) {
            pGuiGraphics.text(Minecraft.getInstance().font, this.getEntityData().getEntityType().getDescription(), 5, 2, 0xFF404040, false);
        }
        pGuiGraphics.text(Minecraft.getInstance().font, Component.translatable("jei.tooltip.cagedmobsreborn.entity.ticks", this.getSeconds()), 10, 102, 0xFF404040, false);
        if(this.ifRequiresWater()){
            pGuiGraphics.text(Minecraft.getInstance().font, Component.translatable("jei.tooltip.cagedmobsreborn.entity.requiresWater", this.getSeconds()).withStyle(ChatFormatting.BLUE), 5, 112, 0xFFFFFFFF, false);
        }
    }

    public List<ItemStack> getSampledSamplers() {
        List<ItemStack> ret = NonNullList.create();
        for (ItemStack stack : samplers) {
            stack = stack.copy();
            EntityType<?> type = entityData.getEntityType();
            CompoundTag nbt = new CompoundTag();
            SerializationHelper.serializeEntityTypeNBT(nbt, type);
            com.corgam.cagedmobsreborn.serializers.SerializationHelper.setCustomData(stack, nbt);

            ret.add(stack);
        }
        return ret;
    }

    private IRecipeSlotRichTooltipCallback getEnvTooltip() {
        return (view, tooltip) -> {
            if(view.getDisplayedItemStack().isPresent()){
                ItemStack displayedItem = view.getDisplayedItemStack().get();
                EnvironmentData env = MobCageBlockEntity.getEnvironmentDataFromItemStack(displayedItem);
                if(env != null){
                    tooltip.add(Component.translatable("jei.tooltip.cagedmobsreborn.entity.growModifier", SIGNED_DECIMAL_FORMAT.format(env.getGrowModifier() * 100 - 100)));
                }
            }
        };
    }

    private IRecipeSlotRichTooltipCallback getLootTooltip(LootData entry) {
        return (view, tooltip) -> {
            if (view.getDisplayedItemStack().isPresent()){
                ItemStack displayedItem = view.getDisplayedItemStack().get();
                tooltip.add(Component.translatable("jei.tooltip.cagedmobsreborn.entity.chance",  DECIMAL_FORMAT.format(entry.getChance() * 100)));
                if(entry.getMinAmount() == entry.getMaxAmount()){
                    tooltip.add(Component.translatable("jei.tooltip.cagedmobsreborn.entity.amountEqual",entry.getMinAmount()));
                }else{
                    tooltip.add(Component.translatable("jei.tooltip.cagedmobsreborn.entity.amount",entry.getMinAmount(), entry.getMaxAmount()));
                }
                if(entry.isLighting()){
                    tooltip.add(Component.translatable("jei.tooltip.cagedmobsreborn.entity.lightning_upgrade").withStyle(ChatFormatting.YELLOW));
                }
                if(entry.isCooking() && displayedItem.getItem().equals(SerializationHelper.ingredientStack(entry.getCookedItem()).getItem())){
                    tooltip.add(Component.translatable("jei.tooltip.cagedmobsreborn.entity.cooking_upgrade").withStyle(ChatFormatting.YELLOW));
                }
                if(entry.isArrow()){
                    tooltip.add(Component.translatable("jei.tooltip.cagedmobsreborn.entity.arrow_upgrade").withStyle(ChatFormatting.YELLOW));
                }
                if(entry.hasColor()){
                    tooltip.add(Component.translatable("jei.tooltip.cagedmobsreborn.entity.colorItem").withStyle(ChatFormatting.YELLOW));
                }
            }
        };
    }
}

