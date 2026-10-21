package com.corgam.cagedmobsreborn.registers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
    private static Item.Properties itemProperties(String id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, id)));
    }
    //Registry
    public static final DeferredRegister<Item> CAGED_ITEMS_REGISTER = DeferredRegister.create(Registries.ITEM, CagedMobsReborn.MOD_ID);

    // CAGES
    public final static DeferredHolder<Item, Item> MOB_CAGE = CAGED_ITEMS_REGISTER.register("mob_cage", () -> new BlockItem(CagedBlocks.MOB_CAGE.get(), itemProperties("mob_cage")));
    public final static DeferredHolder<Item, Item> HOPPING_MOB_CAGE = CAGED_ITEMS_REGISTER.register("hopping_mob_cage", () -> new BlockItem(CagedBlocks.HOPPING_MOB_CAGE.get(), itemProperties("hopping_mob_cage")));
    // SAMPLER
    public final static DeferredHolder<Item, Item> DNA_SAMPLER = CAGED_ITEMS_REGISTER.register("dna_sampler", () -> new DnaSamplerItem(itemProperties("dna_sampler").stacksTo(1)));
    public final static DeferredHolder<Item, Item> DIAMOND_DNA_SAMPLER = CAGED_ITEMS_REGISTER.register("diamond_dna_sampler", () -> new DnaSamplerDiamondItem(itemProperties("diamond_dna_sampler").stacksTo(1)));
    public final static DeferredHolder<Item, Item> NETHERITE_DNA_SAMPLER = CAGED_ITEMS_REGISTER.register("netherite_dna_sampler", () -> new DnaSamplerNetheriteItem(itemProperties("netherite_dna_sampler").stacksTo(1)));
    // UPGRADES
    public final static DeferredHolder<Item, Item> SPEED_I_UPGRADE = CAGED_ITEMS_REGISTER.register("speed_i_upgrade", () -> new SpeedIUpgradeItem(itemProperties("speed_i_upgrade")));
    public final static DeferredHolder<Item, Item> SPEED_II_UPGRADE = CAGED_ITEMS_REGISTER.register("speed_ii_upgrade", () -> new SpeedIIUpgradeItem(itemProperties("speed_ii_upgrade")));
    public final static DeferredHolder<Item, Item> SPEED_III_UPGRADE = CAGED_ITEMS_REGISTER.register("speed_iii_upgrade", () -> new SpeedIIIUpgradeItem(itemProperties("speed_iii_upgrade")));
    public final static DeferredHolder<Item, Item> LOOTING_UPGRADE = CAGED_ITEMS_REGISTER.register("looting_upgrade", () -> new LootingUpgradeItem(itemProperties("looting_upgrade")));
    public final static DeferredHolder<Item, Item> COOKING_UPGRADE = CAGED_ITEMS_REGISTER.register("cooking_upgrade", () -> new CookingUpgradeItem(itemProperties("cooking_upgrade")));
    public final static DeferredHolder<Item, Item> LIGHTNING_UPGRADE = CAGED_ITEMS_REGISTER.register("lightning_upgrade", () -> new LightningUpgradeItem(itemProperties("lightning_upgrade")));
    public final static DeferredHolder<Item, Item> ARROW_UPGRADE = CAGED_ITEMS_REGISTER.register("arrow_upgrade", () -> new ArrowUpgradeItem(itemProperties("arrow_upgrade")));
    public final static DeferredHolder<Item, Item> EXPERIENCE_UPGRADE = CAGED_ITEMS_REGISTER.register("experience_upgrade", () -> new ExperienceUpgradeItem(itemProperties("experience_upgrade")));
    public final static DeferredHolder<Item, Item> CREATIVE_UPGRADE = CAGED_ITEMS_REGISTER.register("creative_upgrade", () -> new CreativeUpgradeItem(itemProperties("creative_upgrade")));

    // MISC
    public final static DeferredHolder<Item, Item> DRAGON_SCALE = CAGED_ITEMS_REGISTER.register("dragon_scale", () -> new Item(itemProperties("dragon_scale").rarity(Rarity.EPIC)));
    public final static DeferredHolder<Item, Item> NETHER_STAR_FRAGMENT = CAGED_ITEMS_REGISTER.register("nether_star_fragment", () -> new FoiledItem(itemProperties("nether_star_fragment").rarity(Rarity.UNCOMMON)));
    public final static DeferredHolder<Item, Item> WARDEN_RECEPTOR = CAGED_ITEMS_REGISTER.register("warden_receptor", () -> new Item(itemProperties("warden_receptor").rarity(Rarity.EPIC)));
    public final static DeferredHolder<Item, Item> SPONGE_FRAGMENT = CAGED_ITEMS_REGISTER.register("sponge_fragment", () -> new Item(itemProperties("sponge_fragment")));
    public final static DeferredHolder<Item, Item> HONEY_DROP = CAGED_ITEMS_REGISTER.register("honey_drop", () -> new Item(itemProperties("honey_drop")));
    public final static DeferredHolder<Item, Item> MILK_DROP = CAGED_ITEMS_REGISTER.register("milk_drop", () -> new Item(itemProperties("milk_drop")));
    public final static DeferredHolder<Item, Item> CRYSTALLIZED_EXPERIENCE = CAGED_ITEMS_REGISTER.register("crystallized_experience", () -> new CrystallizedExperienceItem(itemProperties("crystallized_experience")));
    public final static DeferredHolder<Item, Item> EMPTY_SPAWN_EGG = CAGED_ITEMS_REGISTER.register("empty_spawn_egg", () -> new EmptySpawnEggItem(itemProperties("empty_spawn_egg")));
    public final static DeferredHolder<Item, Item> STAR_INFUSED_NETHERITE_INGOT = CAGED_ITEMS_REGISTER.register("star_infused_netherite_ingot", () -> new FoiledItem(itemProperties("star_infused_netherite_ingot").rarity(Rarity.UNCOMMON)));
    public final static DeferredHolder<Item, Item> STAR_INFUSED_NETHERITE_NUGGET = CAGED_ITEMS_REGISTER.register("star_infused_netherite_nugget", () -> new FoiledItem(itemProperties("star_infused_netherite_nugget").rarity(Rarity.UNCOMMON)));

    // BLOCKS
    public final static DeferredHolder<Item, Item> STAR_INFUSED_NETHERITE_BLOCK = CAGED_ITEMS_REGISTER.register("star_infused_netherite_block", () -> new BlockItem(CagedBlocks.STAR_INFUSED_NETHERITE_BLOCK.get(), itemProperties("star_infused_netherite_block").rarity(Rarity.UNCOMMON)));
    public final static DeferredHolder<Item, Item> CRYSTALLIZED_EXPERIENCE_BLOCK = CAGED_ITEMS_REGISTER.register("crystallized_experience_block", () -> new CrystallizedExperienceBlockItem(CagedBlocks.CRYSTALLIZED_EXPERIENCE_BLOCK.get(), itemProperties("crystallized_experience_block")));
}
