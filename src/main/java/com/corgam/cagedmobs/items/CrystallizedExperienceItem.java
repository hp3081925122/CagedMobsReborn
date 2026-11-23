package com.corgam.cagedmobsreborn.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class CrystallizedExperienceItem extends Item {

    public CrystallizedExperienceItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.cagedmobsreborn.crystallized_experience.info").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.cagedmobsreborn.crystallized_experience.info2").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.cagedmobsreborn.crystallized_experience.info3").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public net.minecraft.world.InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if(itemStack.getItem() instanceof CrystallizedExperienceItem){
            // If on client side, just play the sound
            if(level.isClientSide()){
                level.playSound(player, player.getX(), player.getY()+0.5,player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.35F + 0.9F);
            // Do the logic on server side
            }else{
                // Consume the whole stack
                if (player.isCrouching()) {
                    for(int i = 0; i < itemStack.getCount(); i++){
                        player.giveExperiencePoints(level.getRandom().nextInt(2) + 1);
                    }
                    if(!player.isCreative()) {
                        itemStack.setCount(0);
                    }
                    // Consume single item
                } else {
                    if(!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                    player.giveExperiencePoints(level.getRandom().nextInt(2) + 1);
                }
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return net.minecraft.world.InteractionResult.FAIL;
    }

}
