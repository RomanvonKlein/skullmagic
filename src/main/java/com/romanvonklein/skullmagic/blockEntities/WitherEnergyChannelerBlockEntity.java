package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.essence.EssencePool;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class WitherEnergyChannelerBlockEntity extends AEssenceConsumer {

    private int lastTickRedstonePower = 0;
    private static int essenceCost = 200;
    private static final int range = 5;

    public WitherEnergyChannelerBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.WITHER_ENERGY_CHANNELER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, WitherEnergyChannelerBlockEntity be) {
        if (!world.isClient) {
            int power = world.getReceivedRedstonePower(pos);
            if (power > 0 && be.lastTickRedstonePower == 0) {
                EssencePool pool = SkullMagic.essenceManager.getEssencePoolForConsumer(world.getRegistryKey(), pos);
                if (pool != null && pool.linkedPlayerID != null) {
                    PlayerEntity player = world.getPlayerByUuid(pool.linkedPlayerID);
                    if (pool.getEssence() >= WitherEnergyChannelerBlockEntity.essenceCost && player != null) {
                        Direction target = Direction.UP;
                        if (state.contains(Properties.FACING)) {
                            target = state.get(Properties.FACING);
                        }

                        BlockPos skull = null;
                        for (int i = 1; i < range + 1; i++) {
                            BlockPos toTest = new BlockPos(pos.add(target.getVector().multiply(i)));

                            String blockIdentifier = Registry.BLOCK
                                    .getId(world.getBlockState(toTest).getBlock()).toString();
                            if (Config.getConfig().skulls.containsKey(blockIdentifier)) {
                                skull = toTest;
                                break;
                            }
                        }
                        if (skull != null) {
                            pool.discharge(essenceCost);
                            world.setBlockState(skull, Blocks.WITHER_SKELETON_SKULL.getDefaultState());
                            world.playSound(skull.getX(), skull.getY(), skull.getZ(),
                                    SoundEvents.ENTITY_WITHER_SKELETON_DEATH, SoundCategory.BLOCKS, 1.0f, 1.0f, true);
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
