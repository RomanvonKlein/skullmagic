package com.romanvonklein.skullmagic.spells;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MoundSummonSpell extends Spell {

    protected MoundSummonSpell() {
        super(15000, 100, 20, true);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        HitResult result = player.raycast(100, 1, false);
        if (result != null) {
            int radius = (int) (1 + powerLevel * 2);
            Vec3d center = result.getPos();
            World world = player.getWorld();
            boolean placedAnything = false;
            for (int x = (int) Math.round(center.getX() - radius); x < center.getX() + radius; x++) {
                for (int y = (int) Math.round(center.getY() - radius); y < center.getY()
                        + radius; y++) {
                    for (int z = (int) Math.round(center.getZ() - radius); z < center.getZ()
                            + radius; z++) {
                        BlockPos candidate = new BlockPos(x, y, z);
                        if (candidate.isWithinDistance(center, radius)) {
                            if (world.canPlayerModifyAt(player, candidate)
                                    && world.getBlockState(candidate).isAir()) {
                                BlockState state = Blocks.DIRT.getDefaultState();// TODO: grass on top,
                                                                                 // stone at the bottom?
                                if (world.canPlace(state, candidate, null)) {
                                    world.setBlockState(candidate, state);
                                    placedAnything = true;
                                }
                            }
                        }
                    }
                }
            }
            if (placedAnything) {
                world.playSound(null,
                        center.x,
                        center.y,
                        center.z,
                        SoundEvents.BLOCK_ROOTED_DIRT_PLACE,
                        SoundCategory.BLOCKS,
                        1.5f,
                        1f);
            }
        }
        return true;
    }

}
