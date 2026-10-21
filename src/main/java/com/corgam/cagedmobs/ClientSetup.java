package com.corgam.cagedmobsreborn;

import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageRenderer;
import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageScreen;
import com.corgam.cagedmobsreborn.registers.CagedBlockEntities;
import com.corgam.cagedmobsreborn.registers.CagedBlocks;
import com.corgam.cagedmobsreborn.registers.CagedContainers;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.minecraft.resources.Identifier;

public class ClientSetup {
    // Set the render type of blocks
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CagedBlockEntities.MOB_CAGE_BLOCK_ENTITY.get(), MobCageRenderer::new);
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CagedContainers.CAGE_CONTAINER.get(), MobCageScreen::new);
    }

    public static void registerConditionalProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "full"), DnaSamplerFullProperty.MAP_CODEC);
    }
}
