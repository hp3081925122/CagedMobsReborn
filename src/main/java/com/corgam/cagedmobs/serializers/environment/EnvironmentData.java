package com.corgam.cagedmobsreborn.serializers.environment;

import net.minecraft.core.registries.Registries;
import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.registers.CagedRecipeSerializers;
import com.corgam.cagedmobsreborn.registers.CagedRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class EnvironmentData implements Recipe<RecipeInput> {

    private Ingredient inputItem;
    private Block renderBlock;
    private float growModifier;
    private List<String> categories;

    public EnvironmentData(Ingredient item, Block renderBlock, float growModifier, List<String> categories){
        this.inputItem = item;
        this.renderBlock = renderBlock;
        this.growModifier = growModifier;
        this.categories = categories;
        if(CagedMobsReborn.LOGGER != null){
            CagedMobsReborn.LOGGER.info("Loaded EnvironmentData recipe");
        }
    }

    @Override
    public boolean matches(RecipeInput inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput pContainer) {
        return ItemStack.EMPTY;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    public ItemStack getResultItem(HolderLookup.Provider pRegistryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return CagedRecipeSerializers.ENVIRONMENT_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return CagedRecipeTypes.ENVIRONMENT_RECIPE.get();
    }

    public Ingredient getInputItem() {
        return this.inputItem;
    }

    public Block getRenderBlock(){
        return this.renderBlock;
    }

    public float getGrowModifier() {
        return this.growModifier;
    }

    public List<String> getCategories() {
        return this.categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void setGrowthModifier(float modifier) {
        this.growModifier = modifier;
    }

    public void setRenderBlock(Block state) {
        this.renderBlock = state;
    }

    public void setInputItem(Ingredient item) {
        this.inputItem = item;
    }

    public boolean isSpecial() {
        return true;
    }

    public boolean showNotification() { return false; }

    public String group() { return ""; }

    public net.minecraft.world.item.crafting.PlacementInfo placementInfo() { return net.minecraft.world.item.crafting.PlacementInfo.NOT_PLACEABLE; }

    public net.minecraft.world.item.crafting.RecipeBookCategory recipeBookCategory() { return net.minecraft.world.item.crafting.RecipeBookCategories.CRAFTING_MISC; }
}
