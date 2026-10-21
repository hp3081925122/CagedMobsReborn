package com.corgam.cagedmobsreborn.items.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class CreativeUpgradeItem extends UpgradeItem{
    public CreativeUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.cagedmobsreborn.creative_upgrade.info").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.cagedmobsreborn.upgrades.attach").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.cagedmobsreborn.creative_upgrade.info2").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack itemStack) {
        return true;
    }
}
