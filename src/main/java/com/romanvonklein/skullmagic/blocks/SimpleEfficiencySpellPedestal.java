package com.romanvonklein.skullmagic.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class SimpleEfficiencySpellPedestal extends ASPellPedestal {

    public SimpleEfficiencySpellPedestal(Settings settings) {
        super(settings, "efficiency");
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}
