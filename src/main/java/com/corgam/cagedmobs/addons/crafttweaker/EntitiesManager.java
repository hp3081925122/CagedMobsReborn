package com.corgam.cagedmobsreborn.addons.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.corgam.cagedmobsreborn.registers.CagedRecipeSerializers;
import com.corgam.cagedmobsreborn.registers.CagedRecipeTypes;
import com.corgam.cagedmobsreborn.serializers.entity.EntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.cagedmobsreborn.EntitiesManager")
public class EntitiesManager implements IRecipeManager<EntityData> {

    // Used for creating new entityRecipe with just one valid environment
    @ZenCodeType.Method
    public CTEntity create(String entityId, int growTicks, boolean requiresWater, int tier, String environment) {
        return this.create(entityId, growTicks, requiresWater, tier, new String[]{environment} );
    }

    // Used for creating new entityRecipe with more valid environments
    @ZenCodeType.Method
    public CTEntity create(String entityId, int growTicks, boolean requiresWater, int tier, String[] environments) {
        final CTEntity entity = new CTEntity(entityId, growTicks, requiresWater, tier, environments );
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, entity.getEntityData(), ""));
        return entity;
    }

    @ZenCodeType.Method
    public CTEntity getEntity(String id){
        ResourceLocation resource = ResourceLocation.tryParse(id);
        if(resource != null) {
            final EntityData recipe = this.getRecipes().stream().filter(holder -> holder.id().equals(resource)).findFirst().orElseThrow().value();
            return new CTEntity(recipe);
        }
        throw new IllegalStateException("[CagedMobsReborn] Invalid CraftTweaker Entity recipe ID: " + id);
    }

    @Override
    public ResourceLocation getBracketResourceLocation () {
        return CagedRecipeSerializers.ENTITY_RECIPE_SERIALIZER.getId();
    }

    @Override
    public RecipeType<EntityData> getRecipeType () {
        return CagedRecipeTypes.ENTITY_RECIPE.get();
    }
}


