package com.corgam.cagedmobsreborn.serializers.entity;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

public class AdditionalLootDataSerializer implements RecipeSerializer<AdditionalLootData> {

    public static final MapCodec<AdditionalLootData> CODEC = RecordCodecBuilder.mapCodec((entityDataInstance) -> entityDataInstance.group(
            // EntityID
            Codec.STRING.fieldOf("entity").orElse("minecraft:pig").forGetter(AdditionalLootData::getEntityId),
            // Loot
            Codec.list(LootData.CODEC).fieldOf("results").orElse(new ArrayList<>()).forGetter(AdditionalLootData::getResults),
            // Remove from entity
            Codec.BOOL.fieldOf("removeFromEntity").orElse(false).forGetter(AdditionalLootData::isRemoveFromEntity)
    ).apply(entityDataInstance, AdditionalLootData::new));

    @Override
    public MapCodec<AdditionalLootData> codec() {
        return CODEC;
    }

    public AdditionalLootData fromNetwork(FriendlyByteBuf buffer) {
        try {
            // EntityId
            final String entityId = buffer.readUtf();
            // Loot data
            final List<LootData> results = new ArrayList<>();
            final int length = buffer.readInt();
            for (int i = 0; i < length; i++) {
                results.add(LootData.deserializeBuffer(buffer));
            }
            // Remove from entity
            final boolean removeFromEntity = buffer.readBoolean();
            // Return final object
            return new AdditionalLootData(entityId, results, removeFromEntity);
        }catch(final Exception e){
            CagedMobsReborn.LOGGER.catching(e);
            throw new IllegalStateException("Failed to read additionalLootData recipe from network buffer.");
        }
    }

    public void toNetwork(FriendlyByteBuf buffer, AdditionalLootData recipe) {
        try {
            // EntityId
            buffer.writeUtf(recipe.getEntityId());
            // Loot data
            buffer.writeInt(recipe.getResults().size());
            for(final LootData data : recipe.getResults()){
                LootData.serializeBuffer(buffer, data);
            }
            // Remove from entity
            buffer.writeBoolean(recipe.isRemoveFromEntity());
        }catch (final Exception e) {
            CagedMobsReborn.LOGGER.catching(e);
            throw new IllegalStateException("Failed to write additionalLootData recipe to the network buffer.");
        }
    }

    @Override
    public StreamCodec<FriendlyByteBuf, AdditionalLootData> streamCodec() {
        return StreamCodec.of(this::toNetwork, this::fromNetwork);
    }
}


