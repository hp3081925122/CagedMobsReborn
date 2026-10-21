package com.corgam.cagedmobsreborn.addons.jade;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

import java.util.ArrayList;
import java.util.List;

public class CagedMobsRebornComponentProvider implements IBlockComponentProvider {

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig pluginConfig) {
        if(!(blockAccessor.getBlockEntity() instanceof MobCageBlockEntity tile)){
            return;
        }

        // Add growth progress
        if(tile.hasEntity() && tile.hasEnvironment()){
            tooltip.add(JadeUI.text(Component.literal(String.format("生长进度：%3.0f%%", tile.getGrowthPercentage() * 100))));
        }
        // Add Environment
        if(tile.hasEnvironment()){
            ItemStack representation = tile.getEnvironmentItemStack();
            if(representation != null){
                tooltip.add(Component.translatable("JADE.tooltip.cagedmobsreborn.cage.environment"));
                tooltip.add(List.of(
                        JadeUI.item(representation, 1.0F),
                        JadeUI.text(representation.getHoverName())));
            }
        }
        // Add Entity
        if(tile.hasEntity()){
            EntityType<?> representation = tile.getEntityType();
            if(representation != null){
                tooltip.add(Component.literal(
                        Component.translatable("JADE.tooltip.cagedmobsreborn.cage.entity").withStyle(ChatFormatting.GRAY).getString() +
                                Component.translatable(representation.getDescriptionId()).withStyle(ChatFormatting.GRAY).getString()));
            }
        }
        // Add Upgrades
        if(tile.hasAnyUpgrades()){
            // Add Upgrade text
            tooltip.add(Component.translatable("TOP.tooltip.cagedmobsreborn.cage.upgrades"));
            // Iterate through upgrades
            List<Element> upgrades = new ArrayList<>();
            for(ItemStack upgrade : tile.getUpgradesAsItemStacks()){
                if(!upgrade.isEmpty()){
                    upgrades.add(JadeUI.item(upgrade));
                }
            }
            // Render a list of upgrades
            tooltip.add(upgrades);
        }
    }

    @Override
    public Identifier getUid() {
        return Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "cagedmobsreborn_jade");
    }
}
