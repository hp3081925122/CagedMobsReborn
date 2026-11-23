package com.corgam.cagedmobsreborn;

import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageRenderer;
import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageScreen;
import com.corgam.cagedmobsreborn.items.DnaSamplerDiamondItem;
import com.corgam.cagedmobsreborn.items.DnaSamplerItem;
import com.corgam.cagedmobsreborn.items.DnaSamplerNetheriteItem;
import com.corgam.cagedmobsreborn.registers.CagedBlockEntities;
import com.corgam.cagedmobsreborn.registers.CagedBlocks;
import com.corgam.cagedmobsreborn.registers.CagedContainers;
import com.corgam.cagedmobsreborn.registers.CagedItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientSetup {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::renderLayerSetup);
        modEventBus.addListener(ClientSetup::registerScreens);
        modEventBus.addListener(ClientSetup::addPropertiesToItems);
    }

    // 客户端安全获取配方管理器，避免公共类直接引用 Minecraft 客户端类
    public static RecipeManager getRecipeManager() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? null : minecraft.player.connection.getRecipeManager();
    }

    public static void renderLayerSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(CagedBlocks.MOB_CAGE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(CagedBlocks.HOPPING_MOB_CAGE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(CagedBlocks.CRYSTALLIZED_EXPERIENCE_BLOCK.get(), RenderType.translucent());
        BlockEntityRenderers.register(CagedBlockEntities.MOB_CAGE_BLOCK_ENTITY.get(), MobCageRenderer::new);
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CagedContainers.CAGE_CONTAINER.get(), MobCageScreen::new);
    }

    public static void addPropertiesToItems(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(CagedItems.DNA_SAMPLER.get(), ResourceLocation.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "full"), (stack, level, entity, seed) -> DnaSamplerItem.containsEntityType(stack) ? 1.0F : 0.0F);
            ItemProperties.register(CagedItems.DIAMOND_DNA_SAMPLER.get(), ResourceLocation.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "full"), (stack, level, entity, seed) -> DnaSamplerDiamondItem.containsEntityType(stack) ? 1.0F : 0.0F);
            ItemProperties.register(CagedItems.NETHERITE_DNA_SAMPLER.get(), ResourceLocation.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "full"), (stack, level, entity, seed) -> DnaSamplerNetheriteItem.containsEntityType(stack) ? 1.0F : 0.0F);
        });
    }
}
