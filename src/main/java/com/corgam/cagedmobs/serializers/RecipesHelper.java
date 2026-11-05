package com.corgam.cagedmobs.serializers;

import net.minecraft.core.registries.Registries;
import com.corgam.cagedmobs.CagedMobsReborn;
import com.corgam.cagedmobs.registers.CagedRecipeTypes;
import com.corgam.cagedmobs.serializers.environment.EnvironmentData;
import com.corgam.cagedmobs.serializers.entity.AdditionalLootData;
import com.corgam.cagedmobs.serializers.entity.EntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

public class RecipesHelper {

    // Some helper functions
    public static List<EntityData> getEntitiesRecipesList() {
        RecipeManager manager = getRecipeManager();
        if (manager != null) {
            List<EntityData> recipes = new ArrayList<>(manager.getAllRecipesFor(CagedRecipeTypes.ENTITY_RECIPE.get()).stream().toList());
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
            return manager.getAllRecipesFor(CagedRecipeTypes.ENVIRONMENT_RECIPE.get()).stream().toList();
        }
        return Collections.emptyList();
    }

    public static List<AdditionalLootData> getAdditionalLootRecipesList() {
        RecipeManager manager = getRecipeManager();
        if (manager != null) {
            return manager.getAllRecipesFor(CagedRecipeTypes.ADDITIONAL_LOOT_RECIPE.get()).stream().toList();
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
            return Minecraft.getInstance().player.connection.getRecipeManager();
        }
        return null;
    }

    private static RecipeManager getRecipeManagerServer() {
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
            Item i = BuiltInRegistries.ITEM.get(ResourceLocation.parse(s));
            if(i != Items.AIR || i != null){
                blacklisted.add(i);
            }
        }
        return blacklisted;
    }
}



