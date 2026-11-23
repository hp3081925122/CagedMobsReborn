package com.corgam.cagedmobsreborn.items;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageBlockEntity;
import com.corgam.cagedmobsreborn.registers.CagedRecipeTypes;
import com.corgam.cagedmobsreborn.serializers.AutoEntityDataHelper;
import com.corgam.cagedmobsreborn.serializers.RecipesHelper;
import com.corgam.cagedmobsreborn.serializers.SerializationHelper;
import com.corgam.cagedmobsreborn.serializers.entity.EntityData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class DnaSamplerItem extends Item {
    public DnaSamplerItem(Properties properties) {
        super(properties);
    }

    // Called on left-click on an entity to get it's sample
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(!CagedMobsReborn.SERVER_CONFIG.areSamplersDisabled()) {
            if (target.level().isClientSide() || !(attacker instanceof Player)) return false;
            Player player = (Player) attacker;
            // Select the hand where the sampler is
            InteractionHand hand;
            if (player.getMainHandItem().equals(stack)) {
                hand = InteractionHand.MAIN_HAND;
            } else if (player.getOffhandItem().equals(stack)) {
                hand = InteractionHand.OFF_HAND;
            } else {
                return false;
            }
            // Try to sample the target
            if (canBeCached(target) && !RecipesHelper.isEntityTypeBlacklisted(target.getType())) {
                if (samplerTierSufficient(stack, target)) {
                    CompoundTag nbt = SerializationHelper.getCustomData(stack);
                    nbt.remove("Color");
                    SerializationHelper.serializeEntityTypeNBT(nbt, target.getType());
                    // If sheep add it's color to nbt
                    if (target instanceof Sheep) {
                        Sheep sheep = (Sheep) target;
                        DyeColor color = sheep.getColor();
                        nbt.putInt("Color", color.getId());
                    }
                    SerializationHelper.setCustomData(stack, nbt);
                    player.setItemInHand(hand, stack);
                    return true;
                } else {
                    player.displayClientMessage(Component.translatable("item.cagedmobsreborn.dna_sampler.not_sufficient").withStyle(ChatFormatting.RED), true);
                }
            } else {
                player.displayClientMessage(Component.translatable("item.cagedmobsreborn.dna_sampler.not_cachable").withStyle(ChatFormatting.RED), true);
            }
        }
        return false;
    }

    // Checks if a sampler's tier is sufficient to sample given entity
    private static boolean samplerTierSufficient(ItemStack stack, Entity target) {
        EntityType<?> type = target.getType();
        boolean sufficient = false;
        for(final EntityData recipe : RecipesHelper.getEntitiesRecipesList()) {
            if(recipe != null) {
                // Check for null exception
                if(recipe.getEntityType() == null){continue;}
                if(recipe.getEntityType().equals(type) && AutoEntityDataHelper.getRequiredSamplerTier(recipe, target) <= getSamplerTierInt(stack.getItem())) {
                    sufficient = true;
                    break;
                }
            }
        }
        return sufficient;
    }

    // Returns a tier number in a form of int from Item
    private static int getSamplerTierInt(Item item) {
        if(item instanceof DnaSamplerNetheriteItem){
            return 3;
        }else if(item instanceof DnaSamplerDiamondItem){
            return 2;
        }else{
            return 1;
        }
    }

    // Check if entity can be cached based on the list of cachable entities
    private static boolean canBeCached(Entity clickedEntity) {
        boolean contains = false;
        for(final EntityData recipe : RecipesHelper.getEntitiesRecipesList()) {
            if(recipe != null) {
                // Check for null exception
                if(recipe.getEntityType() == null){continue;}
                if(recipe.getEntityType().equals(clickedEntity.getType())) {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (player.isCrouching() && DnaSamplerItem.containsEntityType(itemstack)) {
            removeEntityType(itemstack);
            player.swing(hand);
            CagedMobsReborn.LOGGER.debug("[DNA-SAMPLER] Cleared entity sample on crouch-use (clientSide={})", level.isClientSide());
            return InteractionResultHolder.success(itemstack);
        }
        return InteractionResultHolder.fail(itemstack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable net.minecraft.world.level.Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(getTooltip(stack));
        tooltip.add(getInformationForTier().withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.cagedmobsreborn.dna_sampler.makeEmpty").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.cagedmobsreborn.dna_sampler.getBackEntity").withStyle(ChatFormatting.GRAY));
        if(CagedMobsReborn.SERVER_CONFIG.areSamplersDisabled()){
            tooltip.add(Component.translatable("item.cagedmobsreborn.dna_sampler.disabled").withStyle(ChatFormatting.RED));
        }
    }

    private MutableComponent getInformationForTier(){
        if(this instanceof DnaSamplerNetheriteItem){
            return Component.translatable("item.cagedmobsreborn.dna_sampler.tier3Info");
        }else if(this instanceof DnaSamplerDiamondItem){
            return Component.translatable("item.cagedmobsreborn.dna_sampler.tier2Info");
        }else{
            return Component.translatable("item.cagedmobsreborn.dna_sampler.tier1Info");
        }
    }

    private Component getTooltip(ItemStack stack) {
        if(!DnaSamplerItem.containsEntityType(stack)) {
            return Component.translatable("item.cagedmobsreborn.dna_sampler.empty").withStyle(ChatFormatting.YELLOW);
        }else {
            EntityType<?> type = SerializationHelper.deserializeEntityTypeNBT(SerializationHelper.getCustomData(stack));
            // Add the text component
            if(type != null){
                return Component.translatable(type.getDescriptionId()).withStyle(ChatFormatting.YELLOW);
            }else{
                // If not found say Unknown entity for crash prevention
                return Component.translatable("item.cagedmobsreborn.dna_sampler.unknown_entity").withStyle(ChatFormatting.YELLOW);
            }
        }
    }

    public static boolean containsEntityType(ItemStack stack) {
        return !stack.isEmpty() && SerializationHelper.hasCustomData(stack) && SerializationHelper.getCustomData(stack).contains("entity");
    }

    public void removeEntityType(ItemStack stack) {
        CompoundTag tag = SerializationHelper.getCustomData(stack);
        tag.remove("entity");
        tag.remove("Color");
        SerializationHelper.setCustomData(stack, tag);
    }

    public void setEntityTypeFromCage(MobCageBlockEntity cage, ItemStack stack, Player player, InteractionHand hand){
        EntityType<?> type = cage.getEntityType();
        CompoundTag nbt = SerializationHelper.getCustomData(stack);
                    nbt.remove("Color");
        SerializationHelper.serializeEntityTypeNBT(nbt, type);
        // If sheep add it's color to nbt
        if(type.toString().contains("sheep")){
            nbt.putInt("Color",cage.getColor());
        }
        SerializationHelper.setCustomData(stack, nbt);
        player.setItemInHand(hand, stack);
    }

    public EntityType<?> getEntityType(ItemStack stack) {
        if(SerializationHelper.hasCustomData(stack)) {
            return SerializationHelper.deserializeEntityTypeNBT(SerializationHelper.getCustomData(stack));
        }else {
            return null;
        }
    }
}


