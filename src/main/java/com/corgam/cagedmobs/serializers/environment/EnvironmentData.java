package com.corgam.cagedmobsreborn.serializers.environment;

import net.minecraft.core.registries.Registries;
import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.registers.CagedRecipeSerializers;
import com.corgam.cagedmobsreborn.registers.CagedRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

public class EnvironmentData implements Recipe<Container> {
    private net.minecraft.resources.ResourceLocation recipeId = new net.minecraft.resources.ResourceLocation(CagedMobsReborn.MOD_ID, "unregistered" );

    public void setRecipeId(net.minecraft.resources.ResourceLocation id) { this.recipeId = id; }

    @Override
    public net.minecraft.resources.ResourceLocation getId() { return this.recipeId; }

    private Ingredient inputItem;
    private Block renderBlock;
    private float growModifier;
    private List<String> categories;

    public EnvironmentData(Ingredient item, Block renderBlock, float growModifier, List<String> categories){
        this.inputItem = item;
        this.renderBlock = renderBlock;
        this.growModifier = growModifier;
        this.categories = categories;
        // Add the id to the list of loaded recipes
        if(CagedMobsReborn.LOGGER != null && BuiltInRegistries.ITEM.getKey(item.getItems()[0].getItem()) != null){
            CagedMobsReborn.LOGGER.info("Loaded EnvironmentData recipe for input item: " + BuiltInRegistries.ITEM.getKey(item.getItems()[0].getItem()));
        }
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(Container pContainer, RegistryAccess pRegistryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CagedRecipeSerializers.ENVIRONMENT_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
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

    @Override
    public boolean isSpecial() {
        return true;
    }
}

