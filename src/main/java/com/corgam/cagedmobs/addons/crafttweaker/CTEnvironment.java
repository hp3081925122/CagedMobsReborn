package com.corgam.cagedmobsreborn.addons.crafttweaker;

import net.minecraft.core.registries.Registries;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import com.corgam.cagedmobsreborn.serializers.SerializationHelper;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import org.openzen.zencode.java.ZenCodeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

@ZenRegister
@ZenCodeType.Name("mods.cagedmobsreborn.Environment")
public class CTEnvironment {

    private final EnvironmentData environmentData;

    public CTEnvironment(IIngredient item, Block renderBlock, float growModifier, String[] categories) {
        this(new EnvironmentData(item.asVanillaIngredient(), renderBlock, growModifier, new ArrayList<>(Arrays.asList(categories))));
    }

    public CTEnvironment(EnvironmentData envData){
        this.environmentData = envData;
    }

    @ZenCodeType.Method
    public CTEnvironment addCategory(String category) {
        this.environmentData.getCategories().add(category);
        return this;
    }

    @ZenCodeType.Method
    public CTEnvironment removeCategory(String category) {
        HashSet<String> categories = new HashSet<>(this.environmentData.getCategories());
        categories.remove(category);
        this.environmentData.setCategories(new ArrayList<>(categories));
        return this;
    }

    @ZenCodeType.Method
    public CTEnvironment clearCategories() {
        this.environmentData.getCategories().clear();
        return this;
    }

    @ZenCodeType.Method
    public CTEnvironment setInputItem(IIngredient inputIngredient) {
        this.environmentData.setInputItem(inputIngredient.asVanillaIngredient());
        return this;
    }

    @ZenCodeType.Method
    public CTEnvironment setDisplay(Block displayBlock) {
        this.environmentData.setRenderBlock(displayBlock);
        return this;
    }

    @ZenCodeType.Method
    public CTEnvironment setGrowthModifier(float growthModifier) {
        this.environmentData.setGrowthModifier(growthModifier);
        return this;
    }

    public RecipeHolder<EnvironmentData> getEnvironmentData () {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(SerializationHelper.ingredientStack(this.environmentData.getInputItem()).getItem());
        if(itemId != null){
            return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, itemId.withPrefix("environment_data_")), this.environmentData);
        }else{
            String categories = String.join("_", this.environmentData.getCategories());
            return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "environment_data_" + categories)), this.environmentData);
        }
    }
}
