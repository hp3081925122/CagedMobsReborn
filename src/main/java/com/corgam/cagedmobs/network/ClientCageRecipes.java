package com.corgam.cagedmobsreborn.network;

import com.corgam.cagedmobsreborn.serializers.entity.AdditionalLootData;
import com.corgam.cagedmobsreborn.serializers.entity.EntityData;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClientCageRecipes {

    private static List<EntityData> entities = Collections.emptyList();
    private static List<EnvironmentData> environments = Collections.emptyList();
    private static List<AdditionalLootData> additionalLoots = Collections.emptyList();
    private static final List<Runnable> LISTENERS = new ArrayList<>();

    private ClientCageRecipes() {
    }

    public static void update(CageRecipesSyncPayload payload) {
        entities = List.copyOf(payload.entities());
        environments = List.copyOf(payload.environments());
        additionalLoots = List.copyOf(payload.additionalLoots());
        LISTENERS.forEach(Runnable::run);
    }

    public static void addListener(Runnable listener) {
        LISTENERS.add(listener);
    }

    public static List<EntityData> getEntities() {
        return entities;
    }

    public static List<EnvironmentData> getEnvironments() {
        return environments;
    }

    public static List<AdditionalLootData> getAdditionalLoots() {
        return additionalLoots;
    }
}
