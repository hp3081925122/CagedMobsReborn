package com.corgam.cagedmobsreborn.network;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.serializers.entity.AdditionalLootData;
import com.corgam.cagedmobsreborn.serializers.entity.AdditionalLootDataSerializer;
import com.corgam.cagedmobsreborn.serializers.entity.EntityData;
import com.corgam.cagedmobsreborn.serializers.entity.EntityDataSerializer;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentData;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentDataSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record CageRecipesSyncPayload(List<EntityData> entities, List<EnvironmentData> environments, List<AdditionalLootData> additionalLoots) implements CustomPacketPayload {

    public static final Type<CageRecipesSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "cage_recipes_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CageRecipesSyncPayload> STREAM_CODEC = StreamCodec.composite(
            EntityDataSerializer.STREAM_CODEC.apply(ByteBufCodecs.list()), CageRecipesSyncPayload::entities,
            EnvironmentDataSerializer.STREAM_CODEC.apply(ByteBufCodecs.list()), CageRecipesSyncPayload::environments,
            AdditionalLootDataSerializer.STREAM_CODEC.apply(ByteBufCodecs.list()), CageRecipesSyncPayload::additionalLoots,
            CageRecipesSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
