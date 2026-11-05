package com.corgam.cagedmobsreborn.serializers.environment;

import net.minecraft.core.registries.Registries;
import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.serializers.SerializationHelper;
import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentDataSerializer implements RecipeSerializer<EnvironmentData>{

    private static final Codec<EnvironmentData> CODEC = RecordCodecBuilder.create((codecInstance) -> codecInstance.group(
            com.corgam.cagedmobsreborn.serializers.SerializationHelper.INGREDIENT_CODEC.fieldOf("input").forGetter(EnvironmentData::getInputItem),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("render").forGetter(EnvironmentData::getRenderBlock),
            Codec.FLOAT.fieldOf("growModifier").forGetter(EnvironmentData::getGrowModifier),
            Codec.list(Codec.STRING).fieldOf("categories").forGetter(EnvironmentData::getCategories))
            .apply(codecInstance, EnvironmentData::new));

    @Override
    public EnvironmentData fromJson(net.minecraft.resources.ResourceLocation id, com.google.gson.JsonObject json) {
        EnvironmentData recipe = CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json).getOrThrow(false, message -> { throw new com.google.gson.JsonSyntaxException(message); });
        recipe.setRecipeId(id);
        return recipe;
    }

    public EnvironmentData fromNetwork(net.minecraft.resources.ResourceLocation id, FriendlyByteBuf buffer) {
        try {
            // Input item
            final Ingredient inputItem = Ingredient.fromNetwork(buffer);
            // Block to render
            final Block renderBlock = SerializationHelper.deserializeBlock(buffer);
            // Grow modifier
            final float growModifier = buffer.readFloat();
            // Categories
            final List<String> categories = new ArrayList<>();
            SerializationHelper.deserializeStringCollection(buffer, categories);
            // Create the environment data
            EnvironmentData recipe = new EnvironmentData(inputItem, renderBlock, growModifier, categories);
            recipe.setRecipeId(id);
            return recipe;
        }catch(final Exception e){
            CagedMobsReborn.LOGGER.catching(e);
            throw new IllegalStateException("Failed to read environmentData recipe from packet buffer.");
        }
    }

    public void toNetwork(FriendlyByteBuf buffer, EnvironmentData recipe) {
        try{
            // Input item
            recipe.getInputItem().toNetwork(buffer);
            // Block to render
            SerializationHelper.serializeBlock(buffer, recipe.getRenderBlock());
            // Grow modifier
            buffer.writeFloat(recipe.getGrowModifier());
            // Categories
            SerializationHelper.serializeStringCollection(buffer, recipe.getCategories());
        }catch (final Exception e) {
            CagedMobsReborn.LOGGER.catching(e);
            throw new IllegalStateException("Failed to write environmentData recipe to the packet buffer.");
        }
    }

    
}


