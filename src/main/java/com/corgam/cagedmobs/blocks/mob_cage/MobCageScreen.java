package com.corgam.cagedmobsreborn.blocks.mob_cage;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.helpers.EntityRendererHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public class MobCageScreen extends AbstractContainerScreen<MobCageContainer> {

    // The path to the GUI image
    private final Identifier GUI = Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "textures/gui/mob_cage.png");
    private final Identifier UPGRADE_SLOT_OUTLINE = Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "textures/gui/upgrade_slot.png");
    private final Identifier ENVIRONMENT_SLOT_OUTLINE = Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "textures/gui/environment_slot.png");

    private static float rotation = 0.0f;
    private static double yaw = 0;

    /**
     * Creates the cage screen rendered on the client side.
     * @param pMenu container
     * @param pPlayerInventory player inventory
     * @param pTitle screen name
     */
    public MobCageScreen(MobCageContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, 176, 183);
        this.inventoryLabelY = this.imageHeight - 93;
    }

    /**
     * Makes the background darker and renders the item tooltips.
     * @param pGuiGraphics graphics stack
     * @param pMouseX x coord of the mouse
     * @param pMouseY y coord of the mouse
     * @param pPartialTick partial tick
     */
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        BlockEntity blockEntity = this.menu.player.level().getBlockEntity(this.menu.pos);
        if(blockEntity instanceof MobCageBlockEntity cageBE && cageBE.getEntity().isPresent()) {
            Optional<Entity> entity;
            if(cageBE.getColor() != -1){
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("Color", cageBE.getColor());
                entity = EntityRendererHelper.createEntity(this.menu.player.level(), cageBE.getEntity().get().getEntityType(), nbt);
            }else{
                entity = EntityRendererHelper.createEntity(this.menu.player.level(), cageBE.getEntity().get().getEntityType(), null);
            }
            if (entity.isPresent()) {
                rotation = (rotation + 0.5F) % 360.0F;
                EntityRendererHelper.renderEntity(graphics, this.leftPos + 62, this.topPos + 17, this.leftPos + 114, this.topPos + 87, rotation, entity.get());
                yaw = (yaw + 1.5D) % 720.0D;
            }
        }
    }

    /**
     * Renders the main GUI of the cage.
     * @param pGuiGraphics graphics stack
     * @param pPartialTick partial tick
     * @param pMouseX x coord of the mouse
     * @param pMouseY y coord of the mouse
     */
    @Override
    public void extractBackground(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int leftPos = this.leftPos;
        int topPos = this.topPos;
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, leftPos, topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        // Render item outline for environment slot
        Slot envSlot = this.menu.getEnvironmentSlot();
        if(!envSlot.hasItem()){
            pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, ENVIRONMENT_SLOT_OUTLINE, leftPos+envSlot.x, topPos+envSlot.y, 0.0F, 0.0F, 16, 16, 16, 16);
        }
        // Render item outlines for upgrade slots
        int i = 1;
        for(Slot slot : this.menu.getUpgradeSlots()){
            if (!slot.hasItem()) {
                pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, UPGRADE_SLOT_OUTLINE, leftPos+slot.x, topPos+slot.y, 0.0F, 0.0F, 16, 16, 16, 16);
                i++;
            }
        }
    }
}
