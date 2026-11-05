package com.corgam.cagedmobsreborn.addons.jade;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

import java.util.List;

public class HideContainerItemsProvider implements IServerExtensionProvider<MobCageBlockEntity, ItemStack> {

    public static final ResourceLocation UID = new ResourceLocation(CagedMobsReborn.MOD_ID, "hide_container_items");

    @Override
    public @Nullable List<ViewGroup<ItemStack>> getGroups(net.minecraft.server.level.ServerPlayer player, net.minecraft.server.level.ServerLevel level, MobCageBlockEntity cage, boolean showDetails) {
        return List.of();
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }


}


