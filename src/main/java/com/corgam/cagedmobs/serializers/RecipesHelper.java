package com.corgam.cagedmobsreborn.serializers;

import net.minecraft.core.registries.Registries;
import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.registers.CagedRecipeTypes;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentData;
import com.corgam.cagedmobsreborn.serializers.entity.AdditionalLootData;
import com.corgam.cagedmobsreborn.serializers.entity.EntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
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
            return manager.getRecipes().stream().map(RecipeHolder::value).filter(EntityData.class::isInstance).map(EntityData.class::cast).toList();
        }
        return Collections.emptyList();
    }

    public static List<EnvironmentData> getEnvironmentRecipesList() {
        RecipeManager manager = getRecipeManager();
        if (manager != null) {
            return manager.getRecipes().stream().map(RecipeHolder::value).filter(EnvironmentData.class::isInstance).map(EnvironmentData.class::cast).toList();
        }
        return Collections.emptyList();
    }

    public static List<AdditionalLootData> getAdditionalLootRecipesList() {
        RecipeManager manager = getRecipeManager();
        if (manager != null) {
            return manager.getRecipes().stream().map(RecipeHolder::value).filter(AdditionalLootData.class::isInstance).map(AdditionalLootData.class::cast).toList();
        }
        return Collections.emptyList();
    }

    public static RecipeManager getRecipeManager(){
        try{
            if(EffectiveSide.get().isClient()){
                return RecipesHelper.getRecipeManagerClient();
            }else{
                return RecipesHelper.getRecipeManagerServer();
            }
        }catch(final Exception e){
            throw new RuntimeException(e);
        }
    }

    private static RecipeManager getRecipeManagerClient() {
        if(Minecraft.getInstance().player != null){
            return null;
        }
        return null;
    }

    private static RecipeManager getRecipeManagerServer() {
        return ServerLifecycleHooks.getCurrentServer().getRecipeManager();
    }

    public static boolean isEnvValidForEntity(EntityData entity, EnvironmentData env) {
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

    public static List<Item> getItemsFromConfigList(){
        List<Item> blacklisted = new java.util.ArrayList<>(Collections.emptyList());
        List<? extends String> blacklistedItems = CagedMobsReborn.SERVER_CONFIG.getItemsList();
        for(String s : blacklistedItems){
            Item i = BuiltInRegistries.ITEM.get(Identifier.parse(s)).map(holder -> holder.value()).orElse(Items.AIR);
            if(i != Items.AIR){
                blacklisted.add(i);
            }
        }
        return blacklisted;
    }
}
