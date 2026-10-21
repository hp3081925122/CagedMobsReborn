package com.corgam.cagedmobsreborn.registers;

import net.minecraft.core.registries.Registries;
import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CagedBlockEntities {
    //Registry
    public static final DeferredRegister<BlockEntityType<?>> CAGED_BLOCK_ENTITIES_REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CagedMobsReborn.MOD_ID);

    // Cage Entity
    public final static DeferredHolder<BlockEntityType<?>, BlockEntityType<MobCageBlockEntity>> MOB_CAGE_BLOCK_ENTITY = CAGED_BLOCK_ENTITIES_REGISTER.register("mob_cage", () -> BlockEntityType.Builder.of(MobCageBlockEntity::new, CagedBlocks.MOB_CAGE.get(), CagedBlocks.HOPPING_MOB_CAGE.get()).build(null));
}