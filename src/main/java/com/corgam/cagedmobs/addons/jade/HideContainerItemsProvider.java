package com.corgam.cagedmobsreborn.addons.jade;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageBlockEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

import java.util.List;

public class HideContainerItemsProvider implements IServerExtensionProvider<ItemStack> {

    public static final Identifier UID = Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID,"hide_container_items");

    @Override
    public @Nullable List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor) {
        return List.of();
    }

    @Override
    public Identifier getUid() {
        return UID;
    }


}
