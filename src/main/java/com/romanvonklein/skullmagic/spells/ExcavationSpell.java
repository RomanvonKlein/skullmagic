package com.romanvonklein.skullmagic.spells;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class ExcavationSpell extends Spell {

    protected ExcavationSpell() {
        super(15000, 100, 20, true);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        int radius = (int) (1 + powerLevel * 2);
        HitResult result = player.raycast(100, 1, false);
        Vec3d center = result.getPos();
        World world = player.getWorld();
        for (int x = (int) Math.round(center.getX() - radius); x < center.getX() + radius; x++) {
            for (int y = (int) Math.round(center.getY() - radius); y < center.getY()
                    + radius; y++) {
                for (int z = (int) Math.round(center.getZ() - radius); z < center.getZ()
                        + radius; z++) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    if (candidate.isWithinDistance(center, radius)) {
                        BlockState targetBlockState = world.getBlockState(candidate);
                        if (!targetBlockState.getBlock().equals(Blocks.AIR)) {

                            ItemStack toolStack = player.getMainHandStack();
                            Item tool = toolStack.getItem();
                            // is the position legaol
                            boolean canBreakBlock = world.canPlayerModifyAt(player, candidate)
                                    && tool.canMine(targetBlockState, world, candidate, player)
                                    && targetBlockState.getBlock().getHardness() >= 0;
                            // is a tool required
                            if (canBreakBlock && targetBlockState.isToolRequired()) {

                                // does the player have the tool required?
                                if (tool instanceof MiningToolItem miningTool
                                        && miningTool.isSuitableFor(targetBlockState)) {
                                    // does the tool have enough durability?
                                    if (!tool.isDamageable() || toolStack.getMaxDamage() > 1
                                            + toolStack.getDamage()) {
                                        toolStack.damage(1, Random.createLocal(), player);
                                    } else {
                                        canBreakBlock = false;
                                    }
                                } else {
                                    canBreakBlock = false;
                                }
                            }
                            if (canBreakBlock) {
                                world.breakBlock(candidate, true, player);
                            }

                        }
                    }
                }
            }
        }
        return true;
    }
}
