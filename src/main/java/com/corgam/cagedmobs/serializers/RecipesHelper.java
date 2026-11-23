package com.corgam.cagedmobsreborn.serializers;

import net.minecraft.core.registries.Registries;
import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.registers.CagedRecipeTypes;
import com.corgam.cagedmobsreborn.serializers.environment.EnvironmentData;
import com.corgam.cagedmobsreborn.serializers.entity.AdditionalLootData;
import com.corgam.cagedmobsreborn.serializers.entity.EntityData;
import com.corgam.cagedmobsreborn.network.ClientCageRecipes;
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
        if(isClientSide()){
            List<EntityData> recipes = new ArrayList<>(ClientCageRecipes.getEntities());
            recipes.addAll(AutoEntityDataHelper.createAutoEntityData(recipes));
            return recipes;
        }
        RecipeManager manager = getRecipeManager();
        if (manager != null) {
            List<EntityData> recipes = new ArrayList<>(manager.getRecipes().stream().map(RecipeHolder::value).filter(EntityData.class::isInstance).map(EntityData.class::cast).toList());
            recipes.addAll(AutoEntityDataHelper.createAutoEntityData(recipes));
            return recipes;
        }
        return Collections.emptyList();
    }

    public static List<EnvironmentData> getEnvironmentRecipesList() {
        if(isClientSide()){
            return ClientCageRecipes.getEnvironments();
        }
        RecipeManager manager = getRecipeManager();
        if (manager != null) {
            return manager.getRecipes().stream().map(RecipeHolder::value).filter(EnvironmentData.class::isInstance).map(EnvironmentData.class::cast).toList();
        }
        return Collections.emptyList();
    }

    public static List<AdditionalLootData> getAdditionalLootRecipesList() {
        if(isClientSide()){
            return ClientCageRecipes.getAdditionalLoots();
        }
        RecipeManager manager = getRecipeManager();
        if (manager != null) {
            return manager.getRecipes().stream().map(RecipeHolder::value).filter(AdditionalLootData.class::isInstance).map(AdditionalLootData.class::cast).toList();
        }
        return Collections.emptyList();
    }

    private static boolean isClientSide(){
        return EffectiveSide.get().isClient();
    }

    public static RecipeManager getRecipeManager(){
        if(isClientSide() || ServerLifecycleHooks.getCurrentServer() == null){
            return null;
        }
        return ServerLifecycleHooks.getCurrentServer().getRecipeManager();
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
            Optional<EntityType<?>> entityType = SerializationHelper.findEntityType(s);
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
