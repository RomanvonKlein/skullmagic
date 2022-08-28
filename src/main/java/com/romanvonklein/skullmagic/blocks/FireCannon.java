package com.romanvonklein.skullmagic.blocks;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.FireCannonBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FireCannon extends AConsumerBlock {

    public FireCannon(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(Properties.FACING, Direction.UP));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState) this.getDefaultState().with(Properties.FACING,
                ctx.getPlayerLookDirection());
    }
    // @Override
    // public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos
    // pos, ShapeContext context) {
    // TODO: read outline shape from model file?
    // VoxelShape shape = VoxelShapes.cuboid(0.125f, 0f, 0.125f, 0.875f, 1.0f,
    // 0.875f);
    //
    // return shape;
    // }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FireCannonBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need
        // to change that!
        return BlockRenderType.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
            BlockEntityType<T> type) {
        return checkType(type, SkullMagic.FIRE_CANNON_BLOCK_ENTITY,
                (world1, pos, state1, be) -> FireCannonBlockEntity.tick(world1, pos, state1, be));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient) {
            SkullMagic.essenceManager.addConsumer(world.getRegistryKey(), pos, placer.getUuid());
        }
    }

    // @Override
    // public ActionResult onUse(BlockState state, World world, BlockPos pos,
    // PlayerEntity player, Hand hand,
    // BlockHitResult hit) {
    // if (!world.isClient) {
    // SkullMagic.LOGGER.info("Current Essence manager: " +
    // SkullMagic.essenceManager);
    // SkullMagic.essenceManager.trySetLinkedPlayer(player, pos);
    // }
    // return ActionResult.SUCCESS;
    // }

    // @Override
    // public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity
    // player) {
    // super.onBreak(world, pos, state, player);
    // if (!world.isClient) {
    // SkullMagic.essenceManager.removeSkullAltar(world.getRegistryKey(), pos);
    // }
    // }
}
