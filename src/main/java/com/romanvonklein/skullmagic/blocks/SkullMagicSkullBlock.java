package com.romanvonklein.skullmagic.blocks;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class SkullMagicSkullBlock extends AbstractSkullMagicSkullBlock {

    public static final int MAX_ROTATION_INDEX = 15;
    private static final int MAX_ROTATIONS = MAX_ROTATION_INDEX + 1;
    public static final IntProperty ROTATION = Properties.ROTATION;
    public static final VoxelShape SHAPE = VoxelShapes.cuboid(0.25f, 0f, 0.25f, 0.75f, 0.5f, 0.75f);

    public SkullMagicSkullBlock(SkullMagicSkullType skullType, Settings settings) {
        super(skullType, settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(ROTATION, 0));
    }

    @Deprecated
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Deprecated
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        // TODO: read outline shape from model file?

        return SHAPE;
    }

    @Deprecated
    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        float rotation = (ctx.getPlayerYaw() + 180.0f) / 22.5f;
        int rotationValue = Math.round(rotation);
        rotationValue = rotationValue == 16 ? 15 : rotationValue;
        return this.getDefaultState().with(ROTATION, rotationValue);
    }

    @Deprecated
    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(ROTATION, rotation.rotate(state.get(ROTATION), MAX_ROTATIONS));
    }

    @Deprecated
    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.with(ROTATION, mirror.mirror(state.get(ROTATION), MAX_ROTATIONS));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    public interface SkullMagicSkullType {
    }

    public enum SkullMagicType implements SkullMagicSkullType {
        ENDERMAN,
        BLAZE,
        SPIDER;
    }

}
