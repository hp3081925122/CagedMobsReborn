package com.corgam.cagedmobsreborn.serializers;

import net.minecraft.core.registries.Registries;
import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.registers.CagedRecipeTypes;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentData;
import com.corgam.cagedmobsreborn.serializers.entity.AdditionalLootData;
import com.corgam.cagedmobsreborn.serializers.entity.EntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

public class RecipesHelper {

    // Some helper functions
    public static List<EntityData> getEntitiesRecipesList() {
        RecipeManager manager = getRecipeManager();
        if (manager != null) {
            List<EntityData> recipes = new ArrayList<>(manager.getAllRecipesFor(CagedRecipeTypes.ENTITY_RECIPE.get()).stream().map(RecipeHolder::value).toList());
            recipes.addAll(AutoEntityDataHelper.createAutoEntityData(recipes));
            CagedMobsReborn.LOGGER.debug("Entity recipe lookup returned {} recipes", recipes.size());
            return recipes;
        }
        CagedMobsReborn.LOGGER.debug("Entity recipe lookup has no recipe manager");
        return Collections.emptyList();
    }

    public static List<EnvironmentData> getEnvironmentRecipesList() {
        RecipeManager manager = getRecipeManager();
        if (manager != null) {
            return manager.getAllRecipesFor(CagedRecipeTypes.ENVIRONMENT_RECIPE.get()).stream().map(RecipeHolder::value).toList();
        }
        return Collections.emptyList();
    }

    public static List<AdditionalLootData> getAdditionalLootRecipesList() {
        RecipeManager manager = getRecipeManager();
        if (manager != null) {
            return manager.getAllRecipesFor(CagedRecipeTypes.ADDITIONAL_LOOT_RECIPE.get()).stream().map(RecipeHolder::value).toList();
        }
        return Collections.emptyList();
    }

    public static RecipeManager getRecipeManager() {
        try {
            if (EffectiveSide.get().isClient()) {
                return com.corgam.cagedmobsreborn.ClientSetup.getRecipeManager();
            } else {
                return RecipesHelper.getRecipeManagerServer();
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static RecipeManager getRecipeManagerServer() {
        return ServerLifecycleHooks.getCurrentServer() == null ? null : ServerLifecycleHooks.getCurrentServer().getRecipeManager();
    }

    public static final String UNIVERSAL_ENVIRONMENT = "universal";

    public static boolean isUniversalEnvironmentAllowed(EntityType<?> entityType) {
        return !CagedMobsReborn.SERVER_CONFIG.getUniversalEnvironmentBlacklist().contains(String.valueOf(EntityType.getKey(entityType)));
    }

    public static boolean isEnvValidForEntity(EntityData entity, EnvironmentData env) {
        if(env.getCategories().contains(UNIVERSAL_ENVIRONMENT) && isUniversalEnvironmentAllowed(entity.getEntityType())){
            return true;
        }
        for(String s : entity.getEnvironments()){
            for(String s2 : env.getCategories()){
                if(s.matches(s2)){
                    return true;
                }
            }
        }
        return false;
    }

    public static List<EntityType<?>> getEntityTypesFromConfigList(){
        List<EntityType<?>> blacklisted = new java.util.ArrayList<>(Collections.emptyList());
        List<? extends String> blacklistedEntities = CagedMobsReborn.SERVER_CONFIG.getEntitiesList();
        for(String s : blacklistedEntities){
            Optional<EntityType<?>> entityType = EntityType.byString(s);
            entityType.ifPresent(blacklisted::add);
        }
        return blacklisted;
    }

    public static boolean isEntityTypeBlacklisted(EntityType<?> type){
        List<EntityType<?>> list = getEntityTypesFromConfigList();
        if(CagedMobsReborn.SERVER_CONFIG.isEntitiesListInWhitelistMode()){
            return !list.contains(type);
        }else{
            return list.contains(type);
        }
    }

    public static List<Item> getItemsFromConfigList() {
        List<Item> blacklisted = new java.util.ArrayList<>(Collections.emptyList());
        List<? extends String> blacklistedItems = CagedMobsReborn.SERVER_CONFIG.getItemsList();
        for (String s : blacklistedItems) {
            ResourceLocation id = ResourceLocation.tryParse(s);
            if (id == null) {
                CagedMobsReborn.LOGGER.warn("[CONFIG] Ignored invalid item id in config list: {}", s);
                continue;
            }
            Item i = BuiltInRegistries.ITEM.get(id);
            if (i != null && i != Items.AIR) {
                blacklisted.add(i);
            }
        }
        return blacklisted;
    }
}
