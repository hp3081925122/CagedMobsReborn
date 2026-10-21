package com.corgam.cagedmobsreborn.registers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.blocks.*;
import com.corgam.cagedmobsreborn.blocks.mob_cage.HoppingMobCageBlock;
import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CagedBlocks {
    // Registries
    public static final DeferredRegister<Block> CAGED_BLOCKS_REGISTER = DeferredRegister.create(Registries.BLOCK, CagedMobsReborn.MOD_ID);

    // CAGES
    public final static DeferredHolder<Block, Block> MOB_CAGE = CAGED_BLOCKS_REGISTER.register("mob_cage", () -> new MobCageBlock(Block.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "mob_cage"))).mapColor(MapColor.METAL).sound(SoundType.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops()));
    public final static DeferredHolder<Block, Block> HOPPING_MOB_CAGE = CAGED_BLOCKS_REGISTER.register("hopping_mob_cage", () -> new HoppingMobCageBlock(Block.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "hopping_mob_cage"))).mapColor(MapColor.METAL).sound(SoundType.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops()));
    // BLOCKS
    public final static DeferredHolder<Block, Block> STAR_INFUSED_NETHERITE_BLOCK = CAGED_BLOCKS_REGISTER.register("star_infused_netherite_block", () -> new StarInfusedNetheriteBlock(Block.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "star_infused_netherite_block"))).mapColor(MapColor.COLOR_BLACK).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().strength(50.0F, 1200.0F)));
    public final static DeferredHolder<Block, Block> CRYSTALLIZED_EXPERIENCE_BLOCK = CAGED_BLOCKS_REGISTER.register("crystallized_experience_block", () -> new CrystallizedExperienceBlock(Block.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "crystallized_experience_block"))).mapColor(MapColor.COLOR_LIGHT_GREEN).sound(SoundType.SLIME_BLOCK).noOcclusion()));
}
