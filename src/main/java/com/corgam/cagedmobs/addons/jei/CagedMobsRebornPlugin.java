package com.corgam.cagedmobsreborn.addons.jei;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.items.DnaSamplerItem;
import com.corgam.cagedmobsreborn.registers.CagedItems;
import com.corgam.cagedmobsreborn.serializers.RecipesHelper;
import com.corgam.cagedmobsreborn.serializers.entity.EntityData;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import com.corgam.cagedmobsreborn.network.ClientCageRecipes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JeiPlugin
public class CagedMobsRebornPlugin implements IModPlugin {

    public static final IRecipeType<EntityDataWrapper> ENTITY_RECIPE = IRecipeType.create(CagedMobsReborn.MOD_ID, "entity", EntityDataWrapper.class);

    @Override
    public Identifier getPluginUid () {
        return Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "jei");
    }


    @Override
    public void registerRecipeCatalysts (IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(ENTITY_RECIPE, CagedItems.MOB_CAGE.get(), CagedItems.HOPPING_MOB_CAGE.get());
    }

    private static IJeiRuntime runtime;
    private static List<EntityDataWrapper> registeredRecipes = new ArrayList<>();
    private static boolean listenerRegistered = false;

    @Override
    public void registerRecipes (IRecipeRegistration registration) {
        registeredRecipes = createRecipes();
        registration.addRecipes(ENTITY_RECIPE, registeredRecipes);
        if(!listenerRegistered){
            listenerRegistered = true;
            ClientCageRecipes.addListener(CagedMobsRebornPlugin::refreshRecipes);
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    private static List<EntityDataWrapper> createRecipes() {
        final List<EntityData> entities = new ArrayList<>(RecipesHelper.getEntitiesRecipesList());
        List<EntityType<?>> blacklistedEntities = RecipesHelper.getEntityTypesFromConfigList();
        if(!CagedMobsReborn.SERVER_CONFIG.isEntitiesListInWhitelistMode()) {
            entities.removeIf(data -> blacklistedEntities.contains(data.getEntityType()));
        }else{
            entities.removeIf(data -> !blacklistedEntities.contains(data.getEntityType()));
        }
        return entities.stream().map(EntityDataWrapper::new).collect(Collectors.toList());
    }

    private static void refreshRecipes() {
        if(runtime == null){
            return;
        }
        IRecipeManager recipeManager = runtime.getRecipeManager();
        if(!registeredRecipes.isEmpty()){
            recipeManager.hideRecipes(ENTITY_RECIPE, registeredRecipes);
        }
        registeredRecipes = createRecipes();
        recipeManager.addRecipes(ENTITY_RECIPE, registeredRecipes);
        CagedMobsReborn.LOGGER.debug("[RECIPE-SYNC] JEI refreshed with {} entity recipes", registeredRecipes.size());
    }

    @Override
    public void registerCategories (IRecipeCategoryRegistration registration) {
        final IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new EntityDataCategory(guiHelper, ENTITY_RECIPE));
    }

    // Subtype interpreter for samplers

    private static class DnaSamplerSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
        @Override
        public Object getSubtypeData(ItemStack ingredient, UidContext context) {
            if (ingredient.getItem() instanceof DnaSamplerItem sampler) {
                EntityType<?> entityType = sampler.getEntityType(ingredient);
                if (entityType != null) {
                    return sampler.getEntityType(ingredient).toString();
                }
            }
            return null;
        }
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        DnaSamplerSubtypeInterpreter interpreter = new DnaSamplerSubtypeInterpreter();
        registration.registerSubtypeInterpreter(CagedItems.NETHERITE_DNA_SAMPLER.get(), interpreter);
        registration.registerSubtypeInterpreter(CagedItems.DIAMOND_DNA_SAMPLER.get(), interpreter);
        registration.registerSubtypeInterpreter(CagedItems.DNA_SAMPLER.get(), interpreter);
    }

}