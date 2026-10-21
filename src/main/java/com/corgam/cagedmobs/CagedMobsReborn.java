package com.corgam.cagedmobsreborn;

import com.corgam.cagedmobsreborn.configs.ClientConfig;
import com.corgam.cagedmobsreborn.configs.ServerConfig;
import com.corgam.cagedmobsreborn.items.DnaSamplerDiamondItem;
import com.corgam.cagedmobsreborn.items.DnaSamplerItem;
import com.corgam.cagedmobsreborn.items.DnaSamplerNetheriteItem;
import com.corgam.cagedmobsreborn.registers.CagedBlockEntities;
import com.corgam.cagedmobsreborn.registers.CagedBlocks;
import com.corgam.cagedmobsreborn.registers.CagedContainers;
import com.corgam.cagedmobsreborn.registers.CagedCreativeTabs;
import com.corgam.cagedmobsreborn.registers.CagedItems;
import com.corgam.cagedmobsreborn.registers.CagedRecipeSerializers;
import com.corgam.cagedmobsreborn.registers.CagedRecipeTypes;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CagedMobsReborn.MOD_ID)
public final class CagedMobsReborn {
    public static final String MOD_ID = "cagedmobsreborn";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final ClientConfig CLIENT_CONFIG = new ClientConfig();
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();

    public CagedMobsReborn(IEventBus modEventBus, ModContainer modContainer) {
        CagedBlocks.CAGED_BLOCKS_REGISTER.register(modEventBus);
        CagedItems.CAGED_ITEMS_REGISTER.register(modEventBus);
        CagedBlockEntities.CAGED_BLOCK_ENTITIES_REGISTER.register(modEventBus);
        CagedCreativeTabs.CAGED_CREATIVE_TABS_REGISTER.register(modEventBus);
        CagedRecipeTypes.CAGED_RECIPE_TYPES_REGISTER.register(modEventBus);
        CagedRecipeSerializers.CAGED_RECIPE_SERIALIZERS_REGISTER.register(modEventBus);
        CagedContainers.CAGED_MENU_TYPES_REGISTER.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG.getModConfigSpec());
        modContainer.registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getModConfigSpec());
        modEventBus.addListener(ClientSetup::renderLayerSetup);
        modEventBus.addListener(ClientSetup::registerScreens);
        modEventBus.addListener(this::addPropertiesToItems);
        modEventBus.addListener(this::registerCapabilities);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CagedBlockEntities.MOB_CAGE_BLOCK_ENTITY.get(), (cage, side) -> side == null ? cage.getInventoryHandler() : cage.getRestrictedItemHandler());
    }

    private void addPropertiesToItems(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(CagedItems.DNA_SAMPLER.get(), ResourceLocation.fromNamespaceAndPath(MOD_ID, "full"), (stack, level, entity, seed) -> DnaSamplerItem.containsEntityType(stack) ? 1.0F : 0.0F);
            ItemProperties.register(CagedItems.DIAMOND_DNA_SAMPLER.get(), ResourceLocation.fromNamespaceAndPath(MOD_ID, "full"), (stack, level, entity, seed) -> DnaSamplerDiamondItem.containsEntityType(stack) ? 1.0F : 0.0F);
            ItemProperties.register(CagedItems.NETHERITE_DNA_SAMPLER.get(), ResourceLocation.fromNamespaceAndPath(MOD_ID, "full"), (stack, level, entity, seed) -> DnaSamplerNetheriteItem.containsEntityType(stack) ? 1.0F : 0.0F);
        });
    }
}
