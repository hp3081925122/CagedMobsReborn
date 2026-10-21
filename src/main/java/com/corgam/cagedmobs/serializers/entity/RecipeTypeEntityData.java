package com.corgam.cagedmobsreborn.serializers.entity;

import net.minecraft.world.item.crafting.RecipeType;

public class RecipeTypeEntityData implements RecipeType<EntityData> {
    @Override
    public String toString () {
        return "cagedmobsreborn:entity_data";
    }
}

