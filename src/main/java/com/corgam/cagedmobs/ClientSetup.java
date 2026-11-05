package com.corgam.cagedmobsreborn;

import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageRenderer;
import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageScreen;
import com.corgam.cagedmobsreborn.registers.CagedBlockEntities;
import com.corgam.cagedmobsreborn.registers.CagedBlocks;
import com.corgam.cagedmobsreborn.registers.CagedContainers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.gui.screens.MenuScreens;

public class ClientSetup {
    // Set the render type of blocks
    public static void renderLayerSetup(FMLClientSetupEvent event) {
        // Set the render layers for blocks
        ItemBlockRenderTypes.setRenderLayer(CagedBlocks.MOB_CAGE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(CagedBlocks.HOPPING_MOB_CAGE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(CagedBlocks.CRYSTALLIZED_EXPERIENCE_BLOCK.get(), RenderType.translucent());
        // Register the block entities renderers
        BlockEntityRenderers.register(CagedBlockEntities.MOB_CAGE_BLOCK_ENTITY.get(), MobCageRenderer::new);
    }

    public static void registerScreens(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(CagedContainers.CAGE_CONTAINER.get(), MobCageScreen::new));
    }
}


