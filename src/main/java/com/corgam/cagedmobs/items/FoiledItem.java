package com.corgam.cagedmobs.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FoiledItem extends Item {
    public FoiledItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}

