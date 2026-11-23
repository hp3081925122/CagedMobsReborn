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
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.view.ProgressView;

import java.util.ArrayList;
import java.util.List;

public class CagedMobsRebornComponentProvider implements IBlockComponentProvider {

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig pluginConfig) {
        if (!(blockAccessor.getBlockEntity() instanceof MobCageBlockEntity tile)) {
            return;
        }

        // 完整恢复绿色生长进度条显示
        if (tile.hasEntity() && tile.hasEnvironment()) {
            float progress = tile.getGrowthPercentage();
            tooltip.add(JadeUI.progress(new ProgressView(
                    ProgressView.Part.of(progress, 0xFF44AA44),
                    Component.literal(String.format("%3.0f%%", progress * 100)),
                    JadeUI.progressStyle(),
                    BoxStyle.transparent())));
        }
        // 显示当前环境物品
        if (tile.hasEnvironment()) {
            ItemStack representation = tile.getEnvironmentItemStack();
            if (!representation.isEmpty()) {
                tooltip.add(Component.translatable("JADE.tooltip.cagedmobsreborn.cage.environment"));
                tooltip.add(List.of(
                        JadeUI.item(representation, 1.0F),
                        JadeUI.text(representation.getHoverName())));
            }
        }
        // 显示当前生物类型
        if (tile.hasEntity()) {
            EntityType<?> representation = tile.getEntityType();
            if (representation != null) {
                tooltip.add(Component.literal(
                        Component.translatable("JADE.tooltip.cagedmobsreborn.cage.entity").withStyle(ChatFormatting.GRAY).getString() +
                                Component.translatable(representation.getDescriptionId()).withStyle(ChatFormatting.GRAY).getString()));
            }
        }
        // 显示已安装的升级物品
        if (tile.hasAnyUpgrades()) {
            tooltip.add(Component.translatable("TOP.tooltip.cagedmobsreborn.cage.upgrades"));
            List<Element> upgrades = new ArrayList<>();
            for (ItemStack upgrade : tile.getUpgradesAsItemStacks()) {
                if (!upgrade.isEmpty()) {
                    upgrades.add(JadeUI.item(upgrade));
                }
            }
            tooltip.add(upgrades);
        }
    }

    @Override
    public Identifier getUid() {
        return Identifier.fromNamespaceAndPath(CagedMobsReborn.MOD_ID, "cagedmobsreborn_jade");
    }
}
