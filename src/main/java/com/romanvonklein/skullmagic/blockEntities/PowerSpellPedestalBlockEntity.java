package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class PowerSpellPedestalBlockEntity extends ASpellPedestalBlockEntity {
    public PowerSpellPedestalBlockEntity(BlockPos pos,
            BlockState state) {
        super(SkullMagic.POWER_SPELL_PEDESTAL_BLOCK_ENTITY, pos, state);
        this.type = "cooldown";
    }
}
