package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.data.WorldBlockPos;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class WitherEnergyChannelerBlockEntity extends AConsumerBlockEntity {

    private int lastTickRedstonePower = 0;
    private static int essenceCost = 1500;
    private static final int range = 5;

    public WitherEnergyChannelerBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.WITHER_ENERGY_CHANNELER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, WitherEnergyChannelerBlockEntity be) {
        if (!world.isClient) {
            int power = world.getReceivedRedstonePower(pos);
            if (power > 0 && be.lastTickRedstonePower == 0) {
                WorldBlockPos worldPos = new WorldBlockPos(pos, world.getRegistryKey());
                if (SkullMagic.getServerData().canConsumerApply(worldPos, essenceCost)) {
                    Direction target = Direction.UP;
                    if (state.contains(Properties.FACING)) {
                        target = state.get(Properties.FACING);
                    }

                    BlockPos skull = null;
                    for (int i = 1; i < range + 1; i++) {
                        BlockPos toTest = new BlockPos(pos.add(target.getVector().multiply(i)));

                        String blockIdentifier = Registries.BLOCK
                                .getId(world.getBlockState(toTest).getBlock()).toString();
                        if (Config.getConfig().skulls.containsKey(blockIdentifier)) {
                            skull = toTest;
                            break;
                        }
                    }
                    if (skull != null) {
                        SkullMagic.getServerData().applyConsumer(worldPos, essenceCost);
                        world.setBlockState(skull, Blocks.WITHER_SKELETON_SKULL.getDefaultState());
                        world.playSound(null, pos,
                                SoundEvents.ENTITY_WITHER_SKELETON_DEATH, SoundCategory.BLOCKS, 1f, 1f);
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
