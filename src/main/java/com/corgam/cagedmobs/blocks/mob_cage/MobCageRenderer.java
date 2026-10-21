package com.corgam.cagedmobsreborn.blocks.mob_cage;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.registers.CagedItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MobCageRenderer implements BlockEntityRenderer<MobCageBlockEntity, MobCageRenderer.State> {
    public static class State extends BlockEntityRenderState {
        private MobCageBlockEntity blockEntity;
        private EntityRenderState entityState;
        private float scale;
        private double zOffset;
    }

    public MobCageRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MobCageBlockEntity blockEntity, State state, float partialTicks, net.minecraft.world.phys.Vec3 cameraPosition, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
        state.blockEntity = blockEntity;
        state.entityState = null;
        state.scale = 0.0F;
        state.zOffset = 0.0D;
        if(blockEntity.hasEntity() && CagedMobsReborn.CLIENT_CONFIG.shouldEntitiesRender()) {
            Entity entity = blockEntity.getCachedEntity(blockEntity.getLevel());
            if(entity != null) {
                float maxSize = getEntitySize(entity);
                float maxEntityDimension = Math.max(entity.getBbWidth(), entity.getBbHeight());
                if(maxEntityDimension > 1.0F) maxSize /= maxEntityDimension;
                if(CagedMobsReborn.CLIENT_CONFIG.shouldGrowthRender()) {
                    state.scale = blockEntity.getGrowthPercentage() * maxSize;
                    if(blockEntity.hasUpgrades(CagedItems.CREATIVE_UPGRADE.get(), 1)) state.scale = maxSize;
                } else {
                    state.scale = maxSize;
                }
                if(entity instanceof Sheep sheep) sheep.setColor(DyeColor.byId(blockEntity.getColor()));
                state.zOffset = getEntityZ(entity);
                state.entityState = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(entity, partialTicks);
            }
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if(state.entityState == null || state.blockEntity == null) return;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.1D, state.zOffset);
        poseStack.scale(state.scale, state.scale, state.scale);
        Minecraft.getInstance().getEntityRenderDispatcher().submit(state.entityState, camera, 0.0D, 0.0D, 0.0D, poseStack, collector);
        poseStack.popPose();
    }

    private double getEntityZ(Entity entity) {
        if(entity instanceof Dolphin) return 0.25D;
        if(entity instanceof ElderGuardian) return 1.2D;
        if(entity instanceof Guardian) return 0.7D;
        if(entity instanceof Hoglin || entity instanceof Zoglin) return -0.3D;
        return 0.0D;
    }

    private float getEntitySize(Entity entity) {
        if(entity instanceof Dolphin || entity instanceof Panda || entity instanceof Pig || entity instanceof PolarBear || entity instanceof Squid || entity instanceof Turtle || entity instanceof Hoglin || entity instanceof Ravager || entity instanceof Shulker || entity instanceof Zoglin) return 0.32F;
        if(entity instanceof AbstractHorse) return 0.4F;
        if(entity instanceof Guardian || entity instanceof Phantom) return 0.25F;
        if(entity instanceof EnderDragon) return 0.8F;
        return 0.5F;
    }
}
