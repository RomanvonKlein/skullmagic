package com.romanvonklein.skullmagic.blocks;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.SkullPedestalBlockEntity;
import com.romanvonklein.skullmagic.config.Config;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class SkullPedestal extends BlockWithEntity {
    public static final BooleanProperty CONNECTED = BooleanProperty.of("connected");
    public static final int scanRange = 5;
    public static final int scanHeight = 1;

    public SkullPedestal(Settings settings) {
        super(settings);
        // TODO: is this state even used anymore?
        setDefaultState(getStateManager().getDefaultState().with(CONNECTED, false));
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos,
            boolean notify) {
        if (!world.isClient()) {
            // is the updated block on top of the pedestal?
            if (pos.getX() == sourcePos.getX() && pos.getY() + 1 == sourcePos.getY()
                    && pos.getZ() == sourcePos.getZ()) {
                // is the updated block a skull?
                String updatedNeighborBlockIdentifier = Registries.BLOCK
                        .getId(world.getBlockState(sourcePos).getBlock())
                        .toString();
                // Is this pedestal already part of an altar with said block?
                if (Config.getConfig().skulls.containsKey(updatedNeighborBlockIdentifier)) {
                    // skull placed on pedestal?
                    SkullMagic.getServerData().tryLinkSkullPedestalToNearbyAltar((ServerWorld) world, pos);
                } else {
                    SkullMagic.getServerData().removePedestal(((ServerWorld) world), pos);
                }
            }
        }
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need
        // to change that!
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        // TODO: read outline shape from model file?
        VoxelShape shape = VoxelShapes.cuboid(0.125f, 0f, 0.125f, 0.875f, 1.0f, 0.875f);
        return shape;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CONNECTED);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SkullPedestalBlockEntity(pos, state);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            SkullMagic.getServerData().removePedestal((ServerWorld) world, pos);
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        if (!world.isClient) {
            SkullMagic.getServerData().removePedestal((ServerWorld) world, pos);
        }
        super.onDestroyedByExplosion(world, pos, explosion);
    }
}