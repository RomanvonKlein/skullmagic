package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.data.WorldBlockPos;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlockUserBlockEntity extends AConsumerBlockEntity {

    private int lastTickRedstonePower = 0;
    private static int essenceCost = 1500;

    public BlockUserBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.BLOCK_USER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BlockUserBlockEntity be) {
        if (!world.isClient) {
            int power = world.getReceivedRedstonePower(pos);
            if (power > 0 && be.lastTickRedstonePower == 0) {
                WorldBlockPos worldPos = new WorldBlockPos(pos, world.getRegistryKey());
                if (SkullMagic.getServerData().canConsumerApply(worldPos, power)) {
                    // TODO: this is the start of the truly unique code - the surrounding structure
                    // could (and should) be extracted. maybe with some new Class
                    // RedstoneActivatedConsumer extending AConsumerBlockEntity?
                    Direction target = Direction.UP;
                    if (state.contains(Properties.FACING)) {
                        target = state.get(Properties.FACING);
                    }
                    BlockPos targetPos = new BlockPos(pos.add(target.getVector()));
                    BlockState targetState = world.getBlockState(targetPos);
                    if (!targetState.equals(Blocks.AIR.getDefaultState())) {
                        BlockHitResult hit = new BlockHitResult(
                                new Vec3d(targetPos.getX(), targetPos.getX(), targetPos.getZ()),
                                target.getOpposite(), targetPos, false);
                        targetState.onUse(world, null, Hand.MAIN_HAND, hit);// TODO: does this even work?
                    }
                    SkullMagic.getServerData().applyConsumer(worldPos, essenceCost);
                }
            } else {
                world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_BEACON_DEACTIVATE,
                        SoundCategory.BLOCKS, 1.0f, 1.0f, true);
            }
            be.lastTickRedstonePower = power;
        }
    }
}
