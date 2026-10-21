package com.corgam.cagedmobsreborn.blocks.mob_cage;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.addons.theoneprobe.ITopInfoProvider;
import com.corgam.cagedmobsreborn.items.DnaSamplerDiamondItem;
import com.corgam.cagedmobsreborn.items.DnaSamplerItem;
import com.corgam.cagedmobsreborn.items.DnaSamplerNetheriteItem;
import com.corgam.cagedmobsreborn.items.EmptySpawnEggItem;
import com.corgam.cagedmobsreborn.items.upgrades.UpgradeItem;
import com.corgam.cagedmobsreborn.registers.CagedBlockEntities;
import com.corgam.cagedmobsreborn.registers.CagedItems;
import com.corgam.cagedmobsreborn.serializers.RecipesHelper;
//import mcjty.theoneprobe.api.IProbeHitData;
//import mcjty.theoneprobe.api.IProbeInfo;
//import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageBlockEntity.ENVIRONMENT_SLOT;

public class MobCageBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, ITopInfoProvider {

    public static final MapCodec<MobCageBlock> CODEC = simpleCodec(MobCageBlock::new);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty HOPPING = BooleanProperty.create("hopping");
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 15.0D, 14.0D);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public MobCageBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(HOPPING, Boolean.valueOf(false)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(WATERLOGGED).add(HOPPING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new MobCageBlockEntity(pPos, pState);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
        // If on client side, skip.
        if(level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MobCageBlockEntity cageBE){
            // Get item in hand
            // Try to add environment
            if(!cageBE.hasEnvironment()){
                // Check if there exists a recipe for given item
                if(MobCageBlockEntity.existsEnvironmentFromItemStack(heldItem)){
                    // Set environment
                    cageBE.setEnvironment(heldItem);
                    if(!player.isCreative()){
                        heldItem.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            // Try to add upgrades
            if(cageBE.acceptsUpgrades()){
                if(heldItem.getItem() instanceof UpgradeItem){
                    cageBE.addUpgrade(heldItem);
                    if(!player.isCreative()){
                        heldItem.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            // Add or remove entity
            if(heldItem.getItem() instanceof DnaSamplerItem sampler){
                if(!CagedMobsReborn.SERVER_CONFIG.areSamplersDisabled()) {
                    if (!cageBE.hasEntity()) {
                        // Check if there exists a recipe for a given entity type,
                        // if the environment is suitable for that entity and if it is not blacklisted.
                        if (cageBE.existsEntityDataFromType(sampler.getEntityType(heldItem))
                                && cageBE.isEnvironmentSuitable(player, sampler.getEntityType(heldItem), state)
                                && !RecipesHelper.isEntityTypeBlacklisted(sampler.getEntityType(heldItem))) {
                            // Add entity
                            cageBE.setEntityFromSampler(sampler.getEntityType(heldItem), heldItem);
                            // Clear the sampler
                            if (!player.isCreative()) {
                                // If single use samplers config is enabled, destroy the sampler. If not, just use its DNA.
                                if (CagedMobsReborn.SERVER_CONFIG.areSamplersSingleUse()) {
                                    if (player instanceof ServerPlayer serverPlayer) {
                                        serverPlayer.onEquippedItemBroken(heldItem.getItem(), hand.asEquipmentSlot());
                                    }
                                    heldItem.shrink(1);
                                } else {
                                    sampler.removeEntityType(heldItem);
                                }
                            }
                            return InteractionResult.SUCCESS;
                        }
                        return InteractionResult.FAIL;
                    // Retrieve entity from the cage
                    } else {
                        if (!DnaSamplerItem.containsEntityType(heldItem) && cageBE.getEntity().isPresent()) {
                            // Check if sampler's tier is sufficient
                            if (cageBE.getEntity().get().getSamplerTier() >= 3 && !(heldItem.getItem() instanceof DnaSamplerNetheriteItem)) {
                                player.sendOverlayMessage(Component.translatable("block.cagedmobsreborn.mob_cage.samplerNotSufficient").withStyle(ChatFormatting.RED));
                                return InteractionResult.FAIL;
                            }
                            if (cageBE.getEntity().get().getSamplerTier() >= 2 && !((heldItem.getItem() instanceof DnaSamplerNetheriteItem) || (heldItem.getItem() instanceof DnaSamplerDiamondItem))) {
                                player.sendOverlayMessage(Component.translatable("block.cagedmobsreborn.mob_cage.samplerNotSufficient").withStyle(ChatFormatting.RED));
                                return InteractionResult.FAIL;
                            }
                            // Get back the entity
                            sampler.setEntityTypeFromCage(cageBE, heldItem, player, hand);
                            cageBE.setChanged();
                        } else {
                            player.sendOverlayMessage(Component.translatable("block.cagedmobsreborn.mob_cage.cageAlreadyUsed").withStyle(ChatFormatting.RED));
                            return InteractionResult.FAIL;
                        }
                        cageBE.removeEntity();
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            // Add entity from spawn egg
            if(heldItem.getItem() instanceof SpawnEggItem spawnEggItem){
                if(!CagedMobsReborn.SERVER_CONFIG.areSpawnEggsDisabled()) {
                    if (!cageBE.hasEntity()) {
                        EntityType<?> entityType = spawnEggItem.getType(heldItem);
                        // Check if there exists a recipe for a given entity type,
                        // if the environment is suitable for that entity and if it is not blacklisted.
                        if (cageBE.existsEntityDataFromType(entityType)
                                && cageBE.isEnvironmentSuitable(player, entityType, state)
                                && !RecipesHelper.isEntityTypeBlacklisted(entityType)) {
                            // Add entity
                            cageBE.setEntityFromSampler(entityType, heldItem);
                            // Clear the sampler
                            if (!player.isCreative()) {
                                heldItem.shrink(1);
                                player.addItem(new ItemStack(CagedItems.EMPTY_SPAWN_EGG.get()));
                            }
                            return InteractionResult.SUCCESS;
                        }
                    } else {
                        player.sendOverlayMessage(Component.translatable("block.cagedmobsreborn.mob_cage.cageAlreadyUsed").withStyle(ChatFormatting.RED));
                    }
                }else{
                    player.sendOverlayMessage(Component.translatable("block.cagedmobsreborn.mob_cage.spawnEggsDisabled").withStyle(ChatFormatting.RED));
                }
                return InteractionResult.CONSUME;
            }
            // Retrieve entity from the cage with empty spawn egg
            if(heldItem.getItem() instanceof EmptySpawnEggItem){
                if(!CagedMobsReborn.SERVER_CONFIG.areSpawnEggsDisabled()){
                    if(cageBE.hasEntity()){
                        SpawnEggItem spawnEgg = SpawnEggItem.byId(cageBE.getEntityType()).map(holder -> holder.value()).filter(SpawnEggItem.class::isInstance).map(SpawnEggItem.class::cast).orElse(null);
                        if(spawnEgg != null){
                            if(!player.isCreative()){
                                player.addItem(new ItemStack(spawnEgg));
                                heldItem.shrink(1);
                            }
                            cageBE.removeEntity();
                            return InteractionResult.SUCCESS;
                        }
                       return InteractionResult.FAIL;
                    }
                }else{
                    player.sendOverlayMessage(Component.translatable("block.cagedmobsreborn.mob_cage.spawnEggsDisabled").withStyle(ChatFormatting.RED));
                    return InteractionResult.FAIL;
                }
            }
            // Try to harvest the cage with sword
            if(heldItem.has(DataComponents.WEAPON)){
                if((!state.getValue(HOPPING) || CagedMobsReborn.SERVER_CONFIG.ifHoppingCagesDisabled()) && cageBE.isWaitingForHarvest()){
                    cageBE.onPlayerHarvest(cageBE.getBlockState());
                    if(!player.isCreative() && !level.isClientSide()){
                        ServerPlayer serverPlayer = (ServerPlayer) player;
                        heldItem.hurtAndBreak(1, (net.minecraft.server.level.ServerLevel) level, serverPlayer, broken -> serverPlayer.onEquippedItemBroken(broken, hand.asEquipmentSlot()));
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            // If crouching remove entity
            if(player.isCrouching()){
                if(cageBE.hasEntity()){
                    cageBE.removeEntity();
                    cageBE.setChanged();
                    return InteractionResult.SUCCESS;
                }
            }
            // Open the GUI
            MenuProvider containerProvider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return state.getValue(HOPPING) ? Component.translatable("block.cagedmobsreborn.hopping_mob_cage") : Component.translatable("block.cagedmobsreborn.mob_cage");
                }
                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
                    AbstractContainerMenu menu = new MobCageContainer(pContainerId, pPlayer, pos);
                    menu.addSlotListener(new ContainerListener() {
                        @Override
                        public void slotChanged(AbstractContainerMenu pContainerToSend, int pDataSlotIndex, ItemStack pStack) {
                            if(pDataSlotIndex == ENVIRONMENT_SLOT){
                                cageBE.updateEnvironment();
                            }
                        }
                        @Override
                        public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {}
                    });
                    return menu;
                }
            };
            // Open the GUI
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.openMenu(containerProvider, cageBE.getBlockPos());
            return InteractionResult.SUCCESS;
        } else {
        throw new IllegalStateException("Mob Cage container provider is missing!");
        }
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, CagedBlockEntities.MOB_CAGE_BLOCK_ENTITY.get(), MobCageBlockEntity::tick);
    }

    /**
     * Called on block remove, should drop all items in the inventory.
     */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean movedByPiston) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof final MobCageBlockEntity tile) {
            tile.dropInventory();
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    // Block item description
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack item, Item.TooltipContext getter, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.cagedmobsreborn.mob_cage.mainInfo").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("block.cagedmobsreborn.mob_cage.rightClickHarvest").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("block.cagedmobsreborn.mob_cage.envInfo").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("block.cagedmobsreborn.mob_cage.upgrading").withStyle(ChatFormatting.GRAY));
    }

    // Block shape
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // Water-logging
    @javax.annotation.Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        return super.getStateForPlacement(pContext).setValue(WATERLOGGED, Boolean.valueOf(flag));
    }

    /**
     * Called when placed or when neighbour is updated.
     */
    protected BlockState updateShape(BlockState pState, LevelReader pLevel, net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pCurrentPos, Direction pFacing, BlockPos pFacingPos, BlockState pFacingState, net.minecraft.util.RandomSource random) {
        if (pState.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }
        return super.updateShape(pState, pLevel, ticks, pCurrentPos, pFacing, pFacingPos, pFacingState, random);
    }

    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    // Mods Support

//    /**
//     * Used for TheOneProbe mod support.
//     */
//    @Override
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
//        if((world.getBlockEntity(data.getPos()) instanceof MobCageBlockEntity tile)) {
//            if (tile.hasEnvironment() && tile.hasEntity()) {
//                probeInfo.progress((int) (tile.getGrowthPercentage() * 100), 100, probeInfo.defaultProgressStyle().suffix("%").filledColor(0xff44AA44).alternateFilledColor(0xff44AA44).backgroundColor(0xff836953));
//            }
//            if (tile.hasEnvironment()) {
//                probeInfo.horizontal().text(Component.translatable("JADE.tooltip.cagedmobsreborn.cage.environment"));
//                ItemStack envItem = tile.getInventoryHandler().getStackInSlot(ENVIRONMENT_SLOT);
//                if(!envItem.isEmpty()){
//                    probeInfo.horizontal().item(envItem).itemLabel(envItem);
//                }
//            }
//            if(tile.hasEntity()){
//                probeInfo.horizontal().text(Component.translatable("JADE.tooltip.cagedmobsreborn.cage.entity").withStyle(ChatFormatting.GRAY).getString() +
//                        Component.translatable(tile.getEntityType().getDescriptionId()).withStyle(ChatFormatting.GRAY).getString());
//            }
//            // Upgrades
//            if(tile.hasAnyUpgrades()){
//                // Add Upgrade text
//                probeInfo.horizontal().text(Component.translatable("TOP.tooltip.cagedmobsreborn.cage.upgrades"));
//                IProbeInfo hor = probeInfo.horizontal();
//                for(ItemStack upgrade : tile.getUpgradesAsItemStacks()){
//                    if(!upgrade.isEmpty()){
//                        hor.item(upgrade);
//                    }
//                }
//            }
//        }
//    }
}
