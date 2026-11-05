package com.corgam.cagedmobs.addons.jade;

import com.corgam.cagedmobs.CagedMobsReborn;
import com.corgam.cagedmobs.blocks.mob_cage.MobCageBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

import java.util.List;

public class HideContainerItemsProvider implements IServerExtensionProvider<ItemStack> {

    public static final ResourceLocation UID = new ResourceLocation(CagedMobsReborn.MOD_ID, "hide_container_items");

    @Override
    public @Nullable List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor) {
        return List.of();
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }


}


