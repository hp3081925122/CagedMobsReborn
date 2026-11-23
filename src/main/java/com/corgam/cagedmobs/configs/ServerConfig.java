package com.corgam.cagedmobsreborn.configs;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.List;

public class ServerConfig {

    private final ModConfigSpec spec;

    private final ModConfigSpec.BooleanValue hoppingCagesDisabled;

    private final ModConfigSpec.BooleanValue entitiesListInWhitelistMode;
    private final ModConfigSpec.ConfigValue<List<? extends String>> entitiesList;

    private final ModConfigSpec.BooleanValue itemsListInWhitelistMode;
    private final ModConfigSpec.ConfigValue<List<? extends String>> itemsList;

    private final ModConfigSpec.BooleanValue singleUseSamplers;
    private final ModConfigSpec.BooleanValue disableSamplers;
    private final ModConfigSpec.BooleanValue disableSpawnEggs;

    private final ModConfigSpec.DoubleValue cagesSpeed;

    private final ModConfigSpec.ConfigValue<List<? extends String>> universalEnvironmentBlacklist;

    private final ModConfigSpec.BooleanValue autoEntityDataEnabled;
    private final ModConfigSpec.ConfigValue<List<? extends String>> autoEntityDataBlacklist;
    private final ModConfigSpec.BooleanValue autoEntityDataExcludeBosses;
    private final ModConfigSpec.BooleanValue samplerTierByCurrentHealth;
    private final ModConfigSpec.DoubleValue normalSamplerMaxHealth;
    private final ModConfigSpec.DoubleValue diamondSamplerMaxHealth;
    private final ModConfigSpec.IntValue autoMinGrowTicks;
    private final ModConfigSpec.IntValue autoMaxGrowTicks;
    private final ModConfigSpec.DoubleValue autoGrowHealthScale;

    public ServerConfig(){
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Server side config for CagedMobsReborn. If changed it will affect the whole server!");
        builder.push("server");

        // Disable hopping cages
        builder.comment("Disables Hopping Cages' automatic harvest, making them work the same as the non-hopping variant.");
        this.hoppingCagesDisabled = builder.define("hoppingCagesDisabled", false);

        // Single use samplers
        builder.comment("Makes all samplers (all tiers) only single use. After a mob is sampled and put into the cage the sampler will break.");
        this.singleUseSamplers = builder.define("singleUseSamplers", false);

        // Disable samplers
        builder.comment("Disables all samplers, requiring the player to use only the spawn eggs.");
        this.disableSamplers = builder.define("disableSamplers", false);

        // Disable Spawn Eggs
        builder.comment("Disables the ability to use spawn eggs on mob cages, requiring the player to use only the samplers.");
        this.disableSpawnEggs = builder.define("disableSpawnEggs", false);

        // Cage speed
        builder.comment("Sets the speed of all cages. The bigger the value the faster the cages will work (by default: 1.00).");
        this.cagesSpeed = builder.defineInRange("cagesSpeed",1.00,0.01,100.00);

        // Entities list
        builder.comment("List of all entities blacklisted from use (in a modid:name format). Players will not be able to put entities from this list into cages nor sample them.\n" +
                "To switch this list into a whitelist, set the 'entitiesListInWhitelistMode' value to true. \n" +
                "Example use: when 'entitiesList' contains [\"minecraft:pig\", \"minecraft:cow\",\"minecraft:rabbit\"] vanilla pig, cow and rabbit will be blacklisted.");
        this.entitiesList = builder.defineList("entitiesList", Collections.emptyList(), it -> it instanceof String);

        builder.comment("Changes the entities list into a whitelist (only entities inside 'entitiesList' will be available). \n" +
                "Example use: when 'entitiesListInWhitelistMode' value is set to true and 'entitiesList' contains [\"minecraft:pig\", \"minecraft:chicken\"] all entities will be blacklisted except vanilla pig and chicken.");
        this.entitiesListInWhitelistMode = builder.define("entitiesListInWhitelistMode", false);

        // Items list
        builder.comment("List of all items blacklisted from use (in a modid:name format).\n" +
                "Items in this list will not be able to be produced from cages, even if they are present in one or more recipes.\n" +
                "To switch this list into a whitelist, set the 'itemsListInWhitelistMode' value to true. \n" +
                "Example use: when 'itemsList' contains [\"minecraft:iron_ingot\", \"minecraft:gold_ingot\", \"minecraft:wither_rose\"] iron ingot, gold ingot and wither rose will not drop from any cage recipe.");
        this.itemsList = builder.defineList("itemsList", Collections.emptyList(), it -> it instanceof String);

        builder.comment("Changes the items list into a whitelist (only items inside 'itemsList' can drop from caged mobs). \n" +
                "Example use: when 'itemsListInWhitelistMode' value is set to true and 'itemsList' contains [\"minecraft:iron_ingot\"] only iron ingots could be obtained from caged entities.");
        this.itemsListInWhitelistMode = builder.define("itemsListInWhitelistMode", false);

        builder.comment("List of entities that can not be put into cages with the Universal Environment (in a modid:name format).\n" +
                "These entities still work with their own normal environments.\n" +
                "Example use: when 'universalEnvironmentBlacklist' contains [\"minecraft:warden\", \"minecraft:wither\"] warden and wither will require their normal environments.");
        this.universalEnvironmentBlacklist = builder.defineList("universalEnvironmentBlacklist", Collections.emptyList(), it -> it instanceof String);

        builder.pop();

        builder.comment("Automatic entity data for living entities that have no entity_data recipe in data packs.",
                "Entities defined by data packs always use their data pack values (drops, sampler tier, environments, grow ticks).");
        builder.push("autoEntityData");

        builder.comment("Enables automatic entity data. Entities without a data pack definition can be sampled and caged,",
                "only work with the Universal Environment and produce drops by rolling their own loot table.");
        this.autoEntityDataEnabled = builder.define("enabled", true);

        builder.comment("List of entities that never get automatic entity data (in a modid:name format).",
                "Example use: [\"minecraft:villager\", \"somemod:rare_mob\"]");
        this.autoEntityDataBlacklist = builder.defineList("blacklist", Collections.emptyList(), it -> it instanceof String);

        builder.comment("Excludes every entity inside the 'c:bosses' entity tag from automatic entity data.");
        this.autoEntityDataExcludeBosses = builder.define("excludeBosses", false);

        builder.comment("If false, the required DNA sampler tier uses the entity's default max health.",
                "If true, the current health of the sampled entity is used instead (damaged mobs need lower tier samplers).",
                "Taking an entity back out of a cage always uses the default max health.");
        this.samplerTierByCurrentHealth = builder.define("samplerTierByCurrentHealth", false);

        builder.comment("Entities with health up to this value can be sampled with the normal DNA Sampler.");
        this.normalSamplerMaxHealth = builder.defineInRange("normalSamplerMaxHealth", 20.0, 0.0, 1000000.0);

        builder.comment("Entities with health up to this value can be sampled with the Diamond DNA Sampler.",
                "Entities above it require the Netherite DNA Sampler. The Netherite DNA Sampler works on every entity.");
        this.diamondSamplerMaxHealth = builder.defineInRange("diamondSamplerMaxHealth", 100.0, 0.0, 1000000.0);

        builder.comment("Grow ticks are calculated as: maxGrowTicks - (maxGrowTicks - minGrowTicks) * e^(-health / growHealthScale).",
                "Low health entities stay close to minGrowTicks, high health entities approach but never exceed maxGrowTicks.");
        this.autoMinGrowTicks = builder.defineInRange("minGrowTicks", 600, 1, 1000000);
        this.autoMaxGrowTicks = builder.defineInRange("maxGrowTicks", 3600, 1, 1000000);
        this.autoGrowHealthScale = builder.defineInRange("growHealthScale", 100.0, 0.01, 1000000.0);

        builder.pop();
        this.spec = builder.build();
    }

    public ModConfigSpec getModConfigSpec() {
        return this.spec;
    }

    public boolean ifHoppingCagesDisabled() {
        return this.hoppingCagesDisabled.get();
    }

    public boolean isEntitiesListInWhitelistMode() {
        return this.entitiesListInWhitelistMode.get();
    }

    public List<? extends String> getEntitiesList(){
        return this.entitiesList.get();
    }

    public boolean isItemsListInWhitelistMode() {
        return this.itemsListInWhitelistMode.get();
    }

    public List<? extends String> getItemsList(){
        return this.itemsList.get();
    }

    public boolean areSamplersSingleUse(){return this.singleUseSamplers.get();}

    public boolean areSamplersDisabled(){return this.disableSamplers.get();}

    public boolean areSpawnEggsDisabled(){return this.disableSpawnEggs.get();}

    public Double getSpeedOfCages(){return this.cagesSpeed.get();}

    public boolean isAutoEntityDataEnabled(){return this.autoEntityDataEnabled.get();}

    public List<? extends String> getAutoEntityDataBlacklist(){return this.autoEntityDataBlacklist.get();}

    public boolean ifAutoEntityDataExcludesBosses(){return this.autoEntityDataExcludeBosses.get();}

    public boolean isSamplerTierByCurrentHealth(){return this.samplerTierByCurrentHealth.get();}

    public double getNormalSamplerMaxHealth(){return this.normalSamplerMaxHealth.get();}

    public double getDiamondSamplerMaxHealth(){return this.diamondSamplerMaxHealth.get();}

    public int getAutoMinGrowTicks(){return this.autoMinGrowTicks.get();}

    public int getAutoMaxGrowTicks(){return this.autoMaxGrowTicks.get();}

    public double getAutoGrowHealthScale(){return this.autoGrowHealthScale.get();}

    public List<? extends String> getUniversalEnvironmentBlacklist(){
        return this.universalEnvironmentBlacklist.get();
    }
}
