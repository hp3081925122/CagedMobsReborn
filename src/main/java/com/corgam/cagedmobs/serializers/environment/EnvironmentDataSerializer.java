package com.corgam.cagedmobsreborn.serializers.environment;

import net.minecraft.core.registries.Registries;
import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.serializers.SerializationHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

public final class EnvironmentDataSerializer {

    public static final MapCodec<EnvironmentData> CODEC = RecordCodecBuilder.mapCodec((codecInstance) -> codecInstance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(EnvironmentData::getInputItem),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("render").forGetter(EnvironmentData::getRenderBlock),
            Codec.FLOAT.fieldOf("growModifier").forGetter(EnvironmentData::getGrowModifier),
            Codec.list(Codec.STRING).fieldOf("categories").forGetter(EnvironmentData::getCategories))
            .apply(codecInstance, EnvironmentData::new));

    private static EnvironmentData fromNetwork(RegistryFriendlyByteBuf buffer) {
        try {
            // Input item
            final Ingredient inputItem = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            // Block to render
            final Block renderBlock = SerializationHelper.deserializeBlock(buffer);
            // Grow modifier
            final float growModifier = buffer.readFloat();
            // Categories
            final List<String> categories = new ArrayList<>();
            SerializationHelper.deserializeStringCollection(buffer, categories);
            // Create the environment data
            return new EnvironmentData(inputItem, renderBlock, growModifier, categories);
        }catch(final Exception e){
            CagedMobsReborn.LOGGER.catching(e);
            throw new IllegalStateException("Failed to read environmentData recipe from packet buffer.");
        }
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, EnvironmentData recipe) {
        try{
            // Input item
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getInputItem());
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

    public static final StreamCodec<RegistryFriendlyByteBuf, EnvironmentData> STREAM_CODEC = StreamCodec.of(EnvironmentDataSerializer::toNetwork, EnvironmentDataSerializer::fromNetwork);
}
