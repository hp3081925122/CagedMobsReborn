package com.corgam.cagedmobsreborn.helpers;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;

import static com.corgam.cagedmobsreborn.CagedMobsReborn.LOGGER;

public class EntityRendererHelper {

    public static Optional<Entity> createEntity(Level level, EntityType<?> entityType, CompoundTag startingNbt) {
        Optional<Entity> entity = Optional.empty();
        try {
            if (entityType == null) {
                return Optional.empty();
            }
            if (startingNbt == null) {
                startingNbt = new CompoundTag();
            }
            startingNbt.putString("id", EntityType.getKey(entityType).toString());
            if (level != null && level.isClientSide()) {
                // 分配虚拟展示实体 ID 并忽略和平模式限制，避免 26.2+ Entity.getId() 崩溃
                Entity created = EntityType.loadEntityRecursive(
                        startingNbt,
                        level,
                        new EntitySpawnRequest(EntitySpawnReason.LOAD, true),
                        BaseSpawner.SET_DISPLAY_ENTITY_ID);
                entity = Optional.ofNullable(created);
            }
        } catch (Exception e) {
            LOGGER.error("[CagedMobsReborn] Rendering entity in GUI failed!", e);
        }
        return entity;
    }

    // 在指定 GUI 矩形区域内居中并旋转渲染实体
    public static void renderEntity(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, float rotation, Entity entity) {
        EntityRenderState state = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(entity, 0.0F);
        state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        state.shadowPieces.clear();
        state.outlineColor = 0;

        float scale = getScaleForEntityType(entity);
        float entityHeight = Math.max(0.5F, state.boundingBoxHeight);
        float entityWidth = Math.max(0.5F, state.boundingBoxWidth);
        float maxBoxW = Math.max(1.0F, (x1 - x0) - 6.0F);
        float maxBoxH = Math.max(1.0F, (y1 - y0) - 8.0F);
        scale = Math.min(scale, Math.min(maxBoxW / entityWidth, maxBoxH / entityHeight));

        // 实体空间平移以垂直居中，配合原版偏移系数微调漂浮/水生生物高度
        float verticalOffset = (getOffsetForEntityType(entity) - 50) / 100.0F;
        Vector3f translation = new Vector3f(0.0F, entityHeight / 2.0F - verticalOffset, 0.0F);
        Quaternionf renderRotation = Axis.ZP.rotationDegrees(180.0F);
        renderRotation.mul(Axis.YP.rotationDegrees(rotation));
        // 将局部 GUI 矩形按当前 pose 矩阵变换到屏幕绝对坐标，避免 JEI 等偏移绘制场景下错位到左上角
        ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformAxisAligned(graphics.pose());
        graphics.entity(state, scale, translation, renderRotation, null, bounds.left(), bounds.top(), bounds.right(), bounds.bottom());
    }

    public static float getScaleForEntityType(Entity entity) {
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        if (entity.getType().toString().contains("twilightforest.ur_ghast")) { return 2.0F; }
        if (entity.getType().toString().contains("greekfantasy.charybdis")) { return 3.0F; }
        if (entity.getType().toString().contains("twilightforest.hydra")) { return 3.2F; }
        if (entity.getType().toString().contains("twilightforest.yeti_alpha")) { return 6.0F; }
        if (entity.getType().toString().contains("minecraft.ender_dragon")) { return 5.0F; }
        if (entity.getType().toString().contains("twilightforest.armored_giant")) { return 6.0F; }
        if (entity.getType().toString().contains("twilightforest.giant_miner")) { return 6.0F; }
        if (entity.getType().toString().contains("twilightforest.mini_ghast")) { return 10.0F; }
        if (entity.getType().toString().contains("outvoted:kraken")) { return 2.0F; }
        if (entity.getType().toString().contains("mowziesmobs:frostmaw")) { return 10.0F; }
        if (entity.getType().toString().contains("alexsmobs:cachalot_whale")) { return 0.2F; }
        if (entity.getType().toString().contains("greekfantasy:giant_boar")) { return 10.0F; }
        if (entity.getType().toString().contains("alexsmobs:crocodile")) { return 6.0F; }
        if (entity.getType().toString().contains("alexsmobs:hammerhead_shark")) { return 6.0F; }
        if (entity.getType().toString().contains("upgrade_aquatic:great_thrasher")) { return 2.0F; }
        if (entity.getType().toString().contains("fireandice:cyclops")) { return 2.0F; }
        if (entity.getType().toString().contains("alexsmobs:laviathan")) { return 0.5F; }
        if (entity.getType().toString().contains("alexsmobs:void_worm")) { return 1.0F; }
        if (entity instanceof ElderGuardian) { return 10.0F; }
        if (entity instanceof AbstractFish) { return 25.0F; }
        if (entity instanceof Ghast) { return 5.2F; }
        if (entity instanceof Sniffer) { return 16F; }
        if (width <= height) {
            if (height >= 3) {
                return 10.0F;
            } else if (height >= 2.5) {
                return 15.0F;
            } else if (height >= 1.9) {
                return 18.0F;
            }
        }
        return 20.0F;
    }

    public static int getOffsetForEntityType(Entity entity) {
        if (entity instanceof Phantom ||
                entity instanceof AbstractFish ||
                entity instanceof EnderDragon ||
                entity instanceof Dolphin ||
                entity instanceof Guardian ||
                entity instanceof Turtle
        ) {
            return 60;
        } else if (entity instanceof Ghast || entity instanceof Squid) {
            return 65;
        }
        return 50;
    }
}
