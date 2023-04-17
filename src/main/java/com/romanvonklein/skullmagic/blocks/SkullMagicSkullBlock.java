package com.romanvonklein.skullmagic.blocks;

import org.jetbrains.annotations.Nullable;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.SkullMagicSkullBlockEntity;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SkullMagicSkullBlock extends SkullMagicAbstractSkullBlock {

    public static final int field_31244 = 15;
    public static final IntProperty ROTATION = Properties.ROTATION;
    protected static final VoxelShape SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);

    public SkullMagicSkullBlock(SkullType skullType, Settings settings) {
        super(skullType, settings);
        this.setDefaultState((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(ROTATION, 0));
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SkullMagicSkullBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState) this.getDefaultState().with(ROTATION,
                MathHelper.floor((double) (ctx.getPlayerYaw() * 16.0f / 360.0f) + 0.5) & 0xF);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState) state.with(ROTATION, rotation.rotate(state.get(ROTATION), 16));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return (BlockState) state.with(ROTATION, mirror.mirror(state.get(ROTATION), 16));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    public static enum SkullType {
        ENDERMAN,
        BLAZE,
        SPIDER;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
            BlockEntityType<T> type) {
        return checkType(type, SkullMagic.SKULL_BLOCK_ENTITY,
                (world1, pos, state1, be) -> SkullMagicSkullBlockEntity.tick(world1, pos, state1, be));
    }

}
