package com.corgam.cagedmobsreborn.addons.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.corgam.cagedmobsreborn.registers.CagedRecipeSerializers;
import com.corgam.cagedmobsreborn.registers.CagedRecipeTypes;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.cagedmobsreborn.EnvironmentsManager")
public class EnvironmentsManager implements IRecipeManager<EnvironmentData> {

    @ZenCodeType.Method
    public CTEnvironment create (IIngredient item, Block renderBlock, float growModifier, String category) {
        return this.create(item, renderBlock, growModifier, new String[]{category});
    }

    @ZenCodeType.Method
    public CTEnvironment create (IIngredient item, Block renderBlock, float growModifier, String[] categories) {
        final CTEnvironment env = new CTEnvironment(item, renderBlock, growModifier, categories);
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, env.getEnvironmentData(), ""));
        return env;
    }

    @ZenCodeType.Method
    public CTEnvironment getEnvironment(String id) {
        Identifier resource = Identifier.tryParse(id);
        if(resource != null){
            final EnvironmentData recipe = this.getRecipes().stream().filter(holder -> holder.id().equals(resource)).findFirst().orElseThrow().value();
            return new CTEnvironment(recipe);
        }
        throw new IllegalStateException("[CagedMobsReborn] Invalid CraftTweaker Environment recipe ID: " + id);
    }

    public Identifier getBracketIdentifier () {
        return CagedRecipeSerializers.ENVIRONMENT_RECIPE_SERIALIZER.getId();
    }

    public RecipeType<EnvironmentData> getRecipeType () {
        return CagedRecipeTypes.ENVIRONMENT_RECIPE.get();
    }
}