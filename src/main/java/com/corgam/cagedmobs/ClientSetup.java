package com.corgam.cagedmobsreborn;

import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageRenderer;
import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageScreen;
import com.corgam.cagedmobsreborn.registers.CagedBlockEntities;
import com.corgam.cagedmobsreborn.registers.CagedBlocks;
import com.corgam.cagedmobsreborn.registers.CagedContainers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import com.corgam.cagedmobsreborn.registers.CagedItems;
import com.corgam.cagedmobsreborn.items.DnaSamplerItem;
import com.corgam.cagedmobsreborn.items.DnaSamplerDiamondItem;
import com.corgam.cagedmobsreborn.items.DnaSamplerNetheriteItem;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.gui.screens.MenuScreens;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = CagedMobsReborn.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static net.minecraft.world.item.crafting.RecipeManager getRecipeManager() {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        return minecraft.player == null ? null : minecraft.player.connection.getRecipeManager();
    }
    // Set the render type of blocks
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void renderLayerSetup(FMLClientSetupEvent event) {
        // Set the render layers for blocks
        ItemBlockRenderTypes.setRenderLayer(CagedBlocks.MOB_CAGE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(CagedBlocks.HOPPING_MOB_CAGE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(CagedBlocks.CRYSTALLIZED_EXPERIENCE_BLOCK.get(), RenderType.translucent());
        // Register the block entities renderers
        BlockEntityRenderers.register(CagedBlockEntities.MOB_CAGE_BLOCK_ENTITY.get(), MobCageRenderer::new);
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(CagedContainers.CAGE_CONTAINER.get(), MobCageScreen::new));
    }
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void addPropertiesToItems(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(CagedItems.DNA_SAMPLER.get(), new ResourceLocation(CagedMobsReborn.MOD_ID, "full"), (stack, level, entity, seed) -> DnaSamplerItem.containsEntityType(stack) ? 1.0F : 0.0F);
            ItemProperties.register(CagedItems.DIAMOND_DNA_SAMPLER.get(), new ResourceLocation(CagedMobsReborn.MOD_ID, "full"), (stack, level, entity, seed) -> DnaSamplerDiamondItem.containsEntityType(stack) ? 1.0F : 0.0F);
            ItemProperties.register(CagedItems.NETHERITE_DNA_SAMPLER.get(), new ResourceLocation(CagedMobsReborn.MOD_ID, "full"), (stack, level, entity, seed) -> DnaSamplerNetheriteItem.containsEntityType(stack) ? 1.0F : 0.0F);
        });
    }
}
