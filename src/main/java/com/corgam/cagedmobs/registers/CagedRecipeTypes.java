package com.corgam.cagedmobsreborn.registers;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentData;
import com.corgam.cagedmobsreborn.serializers.environment.RecipeTypeEnvironmentData;
import com.corgam.cagedmobsreborn.serializers.entity.AdditionalLootData;
import com.corgam.cagedmobsreborn.serializers.entity.EntityData;
import com.corgam.cagedmobsreborn.serializers.entity.RecipeAdditionalLoot;
import com.corgam.cagedmobsreborn.serializers.entity.RecipeTypeEntityData;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CagedRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> CAGED_RECIPE_TYPES_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, CagedMobsReborn.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<EntityData>> ENTITY_RECIPE = CAGED_RECIPE_TYPES_REGISTER.register("entity_data", RecipeTypeEntityData::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<EnvironmentData>> ENVIRONMENT_RECIPE = CAGED_RECIPE_TYPES_REGISTER.register("environment_data", RecipeTypeEnvironmentData::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<AdditionalLootData>> ADDITIONAL_LOOT_RECIPE = CAGED_RECIPE_TYPES_REGISTER.register("additional_loot_data", RecipeAdditionalLoot::new);
}
