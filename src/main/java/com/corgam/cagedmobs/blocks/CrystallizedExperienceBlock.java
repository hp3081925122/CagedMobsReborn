package com.corgam.cagedmobsreborn.blocks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class CrystallizedExperienceBlock extends SlimeBlock {
    public CrystallizedExperienceBlock(Properties props) {
        super(props);
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack item, net.minecraft.world.level.BlockGetter getter, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.cagedmobsreborn.crystallized_experience.info2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.cagedmobsreborn.crystallized_experience.info3").withStyle(ChatFormatting.GRAY));
    }
}


