package com.corgam.cagedmobsreborn.network;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.serializers.entity.AdditionalLootData;
import com.corgam.cagedmobsreborn.serializers.entity.EntityData;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentData;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.List;

public final class CagedNetwork {

    private CagedNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(CageRecipesSyncPayload.TYPE, CageRecipesSyncPayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
            CagedMobsReborn.LOGGER.debug("[RECIPE-SYNC] Client received {} entity, {} environment, {} additional loot recipes", payload.entities().size(), payload.environments().size(), payload.additionalLoots().size());
            ClientCageRecipes.update(payload);
        }));
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        RecipeManager manager = event.getPlayerList().getServer().getRecipeManager();
        List<EntityData> entities = manager.getRecipes().stream().map(RecipeHolder::value).filter(EntityData.class::isInstance).map(EntityData.class::cast).toList();
        List<EnvironmentData> environments = manager.getRecipes().stream().map(RecipeHolder::value).filter(EnvironmentData.class::isInstance).map(EnvironmentData.class::cast).toList();
        List<AdditionalLootData> additionalLoots = manager.getRecipes().stream().map(RecipeHolder::value).filter(AdditionalLootData.class::isInstance).map(AdditionalLootData.class::cast).toList();
        CageRecipesSyncPayload payload = new CageRecipesSyncPayload(entities, environments, additionalLoots);
        event.getRelevantPlayers().forEach(player -> {
            PacketDistributor.sendToPlayer(player, payload);
            CagedMobsReborn.LOGGER.debug("[RECIPE-SYNC] Sent {} entity, {} environment, {} additional loot recipes to {}", entities.size(), environments.size(), additionalLoots.size(), player.getName().getString());
        });
    }
}
