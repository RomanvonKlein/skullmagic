package com.romanvonklein.skullmagic.blocks;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.BlockUserBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockUser extends AConsumerBlock {

    public BlockUser(Settings settings) {
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

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        // TODO: read outline shape from model file?
        VoxelShape shape = VoxelShapes.cuboid(0.125f, 0f, 0.125f, 0.875f, 1.0f, 0.875f);
        return shape;
    }

    @Override
    public BlockUserBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlockUserBlockEntity(pos, state);
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
        return checkType(type, SkullMagic.BLOCK_USER_BLOCK_ENTITY,
                (world1, pos, state1, be) -> BlockUserBlockEntity.tick(world1, pos, state1, be));
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            SkullMagic.getServerData().removeConsumer(world.getRegistryKey(), pos);
        }
        super.onBreak(world, pos, state, player);
    }
}