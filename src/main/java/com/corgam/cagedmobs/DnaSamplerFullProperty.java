package com.corgam.cagedmobsreborn;

import com.corgam.cagedmobsreborn.items.DnaSamplerItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record DnaSamplerFullProperty() implements ConditionalItemModelProperty {
    public static final MapCodec<DnaSamplerFullProperty> MAP_CODEC = MapCodec.unit(new DnaSamplerFullProperty());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
        return DnaSamplerItem.containsEntityType(stack);
    }

    @Override
    public MapCodec<DnaSamplerFullProperty> type() {
        return MAP_CODEC;
    }
}
