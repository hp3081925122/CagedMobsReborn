package com.corgam.cagedmobsreborn.registers;

import net.minecraft.core.registries.Registries;
import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageContainer;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CagedContainers {

    public static final DeferredRegister<MenuType<?>> CAGED_MENU_TYPES_REGISTER = DeferredRegister.create(Registries.MENU, CagedMobsReborn.MOD_ID);

    // Mob Cages
    public static final DeferredHolder<MenuType<?>, MenuType<MobCageContainer>> CAGE_CONTAINER = CAGED_MENU_TYPES_REGISTER.register("mob_cage",
            ()-> IMenuTypeExtension.create(((windowId, inv, extraData) -> new MobCageContainer(windowId, inv.player, extraData.readBlockPos()))));
}
