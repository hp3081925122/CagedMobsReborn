package com.corgam.cagedmobsreborn.addons.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.corgam.cagedmobsreborn.registers.CagedRecipeSerializers;
import com.corgam.cagedmobsreborn.registers.CagedRecipeTypes;
import com.corgam.cagedmobsreborn.serializers.entity.AdditionalLootData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.cagedmobsreborn.AdditionalLootsManager")
public class AdditionalLootsManager implements IRecipeManager<AdditionalLootData> {

    // Used for creating new additionalLootRecipe
    @ZenCodeType.Method
    public CTAdditionalLoot create(String entityId, Boolean removeFromEntity) {
        final CTAdditionalLoot additionalLoot = new CTAdditionalLoot(entityId, removeFromEntity);
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, additionalLoot.getAdditionalLootData(), ""));
        return additionalLoot;
    }

    @ZenCodeType.Method
    public CTAdditionalLoot getAdditionalLoot(String id){
        ResourceLocation resource = ResourceLocation.tryParse(id);
        if(resource != null){
            final AdditionalLootData recipe = java.util.Objects.requireNonNull(this.getRecipes().get(resource), "Unknown recipe: " + resource);
            return new CTAdditionalLoot(recipe);
        }
        throw new IllegalStateException("[CagedMobsReborn] Invalid CraftTweaker Additional Loot Data recipe ID: " + id);
    }

    @Override
    public ResourceLocation getBracketResourceLocation () {
        return CagedRecipeSerializers.ADDITIONAL_LOOT_RECIPE_SERIALIZER.getId();
    }

    @Override
    public RecipeType<AdditionalLootData> getRecipeType () {
        return CagedRecipeTypes.ADDITIONAL_LOOT_RECIPE.get();
    }
}
