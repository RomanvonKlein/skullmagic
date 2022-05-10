package com.romanvonklein.skullmagic.blocks;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import net.minecraft.util.registry.Registry;

public class SkullPedestal extends Block {
    public static final BooleanProperty CONNECTED = BooleanProperty.of("connected");
    public static final int scanRange = 5;
    public static final int scanHeight = 1;

    public SkullPedestal(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(CONNECTED, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        // TODO: read outline shape from model file?
        VoxelShape shape = VoxelShapes.cuboid(0.125f, 0f, 0.125f, 0.875f, 1.0f, 0.875f);
        return shape;
    }

    public void addSkull(World world, BlockPos pos, String skullIdentifier, PlayerEntity player) {
        // SkullAltar altar = getSkullAltarNearby(world, pos);
        BlockPos altarPos = getSkullAltarNearby(world, pos);
        int essenceChargeRate = Config.getConfig().skulls.get(skullIdentifier);
        if (altarPos == null) {
            // player.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE, 100, 1);
            if (!world.isClient) {
                world.playSound(null, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1f, 1f);
            }
        } else {
            SkullMagic.LOGGER.info("altar found at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
            if (!world.isClient) {
                world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1f, 1f);
            }

            // update pedestal charge rate
            world.setBlockState(pos, world.getBlockState(pos).with(CONNECTED, true));
        }
    }

    private static BlockPos getSkullAltarNearby(World world, BlockPos pos) {
        BlockPos altarFound = null;
        outer: for (int x = -scanRange; x < scanRange; x++) {
            for (int y = -scanHeight; y < scanHeight; y++) {
                for (int z = -scanRange; z < scanRange; z++) {
                    // TODO: better way of checking for the right blocktype
                    if (Registry.BLOCK.getId(world.getBlockState(pos.add(x, y, z)).getBlock()).toString()
                            .equals("skullmagic:skull_altar")) {
                        altarFound = pos.add(x, y, z);
                        break outer;
                    }
                }
            }
        }
        return altarFound;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CONNECTED);
    }
}