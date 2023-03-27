package com.romanvonklein.skullmagic.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class SimplePowerSpellPedestal extends ASPellPedestal {

    public SimplePowerSpellPedestal(Settings settings) {
        super(settings, "power", 1);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}
