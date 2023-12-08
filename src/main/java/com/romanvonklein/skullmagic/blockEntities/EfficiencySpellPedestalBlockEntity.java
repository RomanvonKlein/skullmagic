package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class EfficiencySpellPedestalBlockEntity extends ASpellPedestalBlockEntity {

    public static String type = "efficiency";

    public EfficiencySpellPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.EFFICIENCY_SPELL_PEDESTAL_BLOCK_ENTITY, pos, state);
    }

}
