package com.corgam.cagedmobsreborn.items.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class LightningUpgradeItem extends UpgradeItem{
    public LightningUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.cagedmobsreborn.lightning_upgrade.info").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, worldIn, display, tooltip, flagIn);
    }
}
