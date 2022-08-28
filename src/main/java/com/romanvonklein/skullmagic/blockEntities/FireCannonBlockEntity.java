package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.persistantState.EssencePool;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FireCannonBlockEntity extends AEssenceConsumer {

    private int lastTickRedstonePower = 0;
    private int essenceCost = 50;

    public FireCannonBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.FIRE_CANNON_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, FireCannonBlockEntity be) {
        if (!world.isClient) {
            // TODO: move this to abstract base class, similar to spells?
            int power = world.getReceivedRedstonePower(pos);
            if (power > 0 && be.lastTickRedstonePower == 0) {
                EssencePool pool = SkullMagic.essenceManager.getEssencePoolForConsumer(world.getRegistryKey(), pos);
                if (pool != null) {

                    if (pool.getEssence() >= be.essenceCost) {
                        SkullMagic.LOGGER.info("Powered :)!");
                        pool.discharge(be.essenceCost);
                    } else {
                        SkullMagic.LOGGER.info("Insufficient essence in pool!");
                    }
                } else {
                    SkullMagic.LOGGER.info("no action taken, as the consumer is not connected to a pool.");
                }
            }
            be.lastTickRedstonePower = power;
        }
    }

}
