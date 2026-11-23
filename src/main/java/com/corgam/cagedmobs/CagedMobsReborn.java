package com.corgam.cagedmobsreborn;

import com.corgam.cagedmobsreborn.configs.ClientConfig;
import com.corgam.cagedmobsreborn.configs.ServerConfig;
import com.corgam.cagedmobsreborn.network.CagedNetwork;
import com.corgam.cagedmobsreborn.registers.CagedBlockEntities;
import com.corgam.cagedmobsreborn.registers.CagedBlocks;
import com.corgam.cagedmobsreborn.registers.CagedContainers;
import com.corgam.cagedmobsreborn.registers.CagedCreativeTabs;
import com.corgam.cagedmobsreborn.registers.CagedItems;
import com.corgam.cagedmobsreborn.registers.CagedRecipeSerializers;
import com.corgam.cagedmobsreborn.registers.CagedRecipeTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
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
        modContainer.registerConfig(ModConfig.Type.SYNCED, SERVER_CONFIG.getModConfigSpec());
        if (FMLEnvironment.getDist().isClient()) {
            ClientSetup.register(modEventBus);
        }
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(CagedNetwork::registerPayloads);
        NeoForge.EVENT_BUS.addListener(CagedNetwork::onDatapackSync);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // 无方向访问暴露内部存储，有方向访问暴露受限句柄以防止漏斗抽取环境与升级
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                CagedBlockEntities.MOB_CAGE_BLOCK_ENTITY.get(),
                (cage, side) -> side == null ? cage.getInventoryHandler() : cage.getRestrictedItemHandler());
    }
}
