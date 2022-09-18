package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.essence.EssencePool;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlockUserBlockEntity extends AEssenceConsumer {

    private int lastTickRedstonePower = 0;
    private static int essenceCost = 1500;

    public BlockUserBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.BLOCK_USER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BlockUserBlockEntity be) {
        if (!world.isClient) {
            int power = world.getReceivedRedstonePower(pos);
            if (power > 0 && be.lastTickRedstonePower == 0) {
                EssencePool pool = SkullMagic.essenceManager.getEssencePoolForConsumer(world.getRegistryKey(), pos);
                if (pool != null && pool.linkedPlayerID != null) {
                    PlayerEntity player = world.getPlayerByUuid(pool.linkedPlayerID);
                    if (pool.getEssence() >= BlockUserBlockEntity.essenceCost && player != null) {
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
                            targetState.onUse(world, null, Hand.MAIN_HAND, hit);//TODO: does this even work?
                        }
                    }
                }
            } else {
                world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_BEACON_DEACTIVATE,
                        SoundCategory.BLOCKS, 1.0f, 1.0f, true);
            }
            be.lastTickRedstonePower = power;
        }
    }
}
