package com.corgam.cagedmobs.registers;

import net.minecraft.core.registries.Registries;
import com.corgam.cagedmobs.CagedMobsReborn;
import com.corgam.cagedmobs.blocks.mob_cage.MobCageBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CagedBlockEntities {
    //Registry
    public static final DeferredRegister<BlockEntityType<?>> CAGED_BLOCK_ENTITIES_REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CagedMobsReborn.MOD_ID);

    // Cage Entity
    public final static RegistryObject<BlockEntityType<?>, BlockEntityType<MobCageBlockEntity>> MOB_CAGE_BLOCK_ENTITY = CAGED_BLOCK_ENTITIES_REGISTER.register("mob_cage", () -> BlockEntityType.Builder.of(MobCageBlockEntity::new, CagedBlocks.MOB_CAGE.get(), CagedBlocks.HOPPING_MOB_CAGE.get()).build(null));
}

