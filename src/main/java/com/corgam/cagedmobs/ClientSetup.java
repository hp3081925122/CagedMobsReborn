package com.corgam.cagedmobs;

import com.corgam.cagedmobs.blocks.mob_cage.MobCageRenderer;
import com.corgam.cagedmobs.blocks.mob_cage.MobCageScreen;
import com.corgam.cagedmobs.registers.CagedBlockEntities;
import com.corgam.cagedmobs.registers.CagedBlocks;
import com.corgam.cagedmobs.registers.CagedContainers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.RegisterMenuScreensEvent;

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

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CagedContainers.CAGE_CONTAINER.get(), MobCageScreen::new);
    }
}


