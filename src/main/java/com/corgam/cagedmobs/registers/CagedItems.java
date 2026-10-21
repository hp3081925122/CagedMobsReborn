package com.corgam.cagedmobsreborn.registers;

import net.minecraft.core.registries.Registries;
import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.items.*;
import com.corgam.cagedmobsreborn.items.upgrades.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CagedItems {
    //Registry
    public static final DeferredRegister<Item> CAGED_ITEMS_REGISTER = DeferredRegister.create(Registries.ITEM, CagedMobsReborn.MOD_ID);

    // CAGES
    public final static DeferredHolder<Item, Item> MOB_CAGE = CAGED_ITEMS_REGISTER.register("mob_cage", () -> new BlockItem(CagedBlocks.MOB_CAGE.get(), new Item.Properties()));
    public final static DeferredHolder<Item, Item> HOPPING_MOB_CAGE = CAGED_ITEMS_REGISTER.register("hopping_mob_cage", () -> new BlockItem(CagedBlocks.HOPPING_MOB_CAGE.get(), new Item.Properties()));
    // SAMPLER
    public final static DeferredHolder<Item, Item> DNA_SAMPLER = CAGED_ITEMS_REGISTER.register("dna_sampler", () -> new DnaSamplerItem(new Item.Properties().stacksTo(1)));
    public final static DeferredHolder<Item, Item> DIAMOND_DNA_SAMPLER = CAGED_ITEMS_REGISTER.register("diamond_dna_sampler", () -> new DnaSamplerDiamondItem(new Item.Properties().stacksTo(1)));
    public final static DeferredHolder<Item, Item> NETHERITE_DNA_SAMPLER = CAGED_ITEMS_REGISTER.register("netherite_dna_sampler", () -> new DnaSamplerNetheriteItem(new Item.Properties().stacksTo(1)));
    // UPGRADES
    public final static DeferredHolder<Item, Item> SPEED_I_UPGRADE = CAGED_ITEMS_REGISTER.register("speed_i_upgrade", () -> new SpeedIUpgradeItem(new Item.Properties()));
    public final static DeferredHolder<Item, Item> SPEED_II_UPGRADE = CAGED_ITEMS_REGISTER.register("speed_ii_upgrade", () -> new SpeedIIUpgradeItem(new Item.Properties()));
    public final static DeferredHolder<Item, Item> SPEED_III_UPGRADE = CAGED_ITEMS_REGISTER.register("speed_iii_upgrade", () -> new SpeedIIIUpgradeItem(new Item.Properties()));
    public final static DeferredHolder<Item, Item> LOOTING_UPGRADE = CAGED_ITEMS_REGISTER.register("looting_upgrade", () -> new LootingUpgradeItem(new Item.Properties()));
    public final static DeferredHolder<Item, Item> COOKING_UPGRADE = CAGED_ITEMS_REGISTER.register("cooking_upgrade", () -> new CookingUpgradeItem(new Item.Properties()));
    public final static DeferredHolder<Item, Item> LIGHTNING_UPGRADE = CAGED_ITEMS_REGISTER.register("lightning_upgrade", () -> new LightningUpgradeItem(new Item.Properties()));
    public final static DeferredHolder<Item, Item> ARROW_UPGRADE = CAGED_ITEMS_REGISTER.register("arrow_upgrade", () -> new ArrowUpgradeItem(new Item.Properties()));
    public final static DeferredHolder<Item, Item> EXPERIENCE_UPGRADE = CAGED_ITEMS_REGISTER.register("experience_upgrade", () -> new ExperienceUpgradeItem(new Item.Properties()));
    public final static DeferredHolder<Item, Item> CREATIVE_UPGRADE = CAGED_ITEMS_REGISTER.register("creative_upgrade", () -> new CreativeUpgradeItem(new Item.Properties()));

    // MISC
    public final static DeferredHolder<Item, Item> DRAGON_SCALE = CAGED_ITEMS_REGISTER.register("dragon_scale", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public final static DeferredHolder<Item, Item> NETHER_STAR_FRAGMENT = CAGED_ITEMS_REGISTER.register("nether_star_fragment", () -> new FoiledItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public final static DeferredHolder<Item, Item> WARDEN_RECEPTOR = CAGED_ITEMS_REGISTER.register("warden_receptor", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public final static DeferredHolder<Item, Item> SPONGE_FRAGMENT = CAGED_ITEMS_REGISTER.register("sponge_fragment", () -> new Item(new Item.Properties()));
    public final static DeferredHolder<Item, Item> HONEY_DROP = CAGED_ITEMS_REGISTER.register("honey_drop", () -> new Item(new Item.Properties()));
    public final static DeferredHolder<Item, Item> MILK_DROP = CAGED_ITEMS_REGISTER.register("milk_drop", () -> new Item(new Item.Properties()));
    public final static DeferredHolder<Item, Item> CRYSTALLIZED_EXPERIENCE = CAGED_ITEMS_REGISTER.register("crystallized_experience", () -> new CrystallizedExperienceItem(new Item.Properties()));
    public final static DeferredHolder<Item, Item> EMPTY_SPAWN_EGG = CAGED_ITEMS_REGISTER.register("empty_spawn_egg", () -> new EmptySpawnEggItem(new Item.Properties()));
    public final static DeferredHolder<Item, Item> STAR_INFUSED_NETHERITE_INGOT = CAGED_ITEMS_REGISTER.register("star_infused_netherite_ingot", () -> new FoiledItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public final static DeferredHolder<Item, Item> STAR_INFUSED_NETHERITE_NUGGET = CAGED_ITEMS_REGISTER.register("star_infused_netherite_nugget", () -> new FoiledItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // BLOCKS
    public final static DeferredHolder<Item, Item> STAR_INFUSED_NETHERITE_BLOCK = CAGED_ITEMS_REGISTER.register("star_infused_netherite_block", () -> new BlockItem(CagedBlocks.STAR_INFUSED_NETHERITE_BLOCK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public final static DeferredHolder<Item, Item> CRYSTALLIZED_EXPERIENCE_BLOCK = CAGED_ITEMS_REGISTER.register("crystallized_experience_block", () -> new CrystallizedExperienceBlockItem(CagedBlocks.CRYSTALLIZED_EXPERIENCE_BLOCK.get(), new Item.Properties()));
}
