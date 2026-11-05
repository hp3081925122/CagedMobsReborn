package com.corgam.cagedmobsreborn.registers;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentData;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentDataSerializer;
import com.corgam.cagedmobsreborn.serializers.entity.AdditionalLootData;
import com.corgam.cagedmobsreborn.serializers.entity.AdditionalLootDataSerializer;
import com.corgam.cagedmobsreborn.serializers.entity.EntityData;
import com.corgam.cagedmobsreborn.serializers.entity.EntityDataSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CagedRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> CAGED_RECIPE_SERIALIZERS_REGISTER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, CagedMobsReborn.MOD_ID);

    public static final RegistryObject<RecipeSerializer<EntityData>> ENTITY_RECIPE_SERIALIZER = CAGED_RECIPE_SERIALIZERS_REGISTER.register("entity_data", EntityDataSerializer::new);
    public static final RegistryObject<RecipeSerializer<EnvironmentData>> ENVIRONMENT_RECIPE_SERIALIZER = CAGED_RECIPE_SERIALIZERS_REGISTER.register("environment_data", EnvironmentDataSerializer::new);
    public static final RegistryObject<RecipeSerializer<AdditionalLootData>> ADDITIONAL_LOOT_RECIPE_SERIALIZER = CAGED_RECIPE_SERIALIZERS_REGISTER.register("additional_loot_data", AdditionalLootDataSerializer::new);

}


