package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class CooldownSpellPedestalBlockEntity extends ASpellPedestalBlockEntity {

    public static String type = "cooldown";

    public CooldownSpellPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.COOLDOWN_SPELL_PEDESTAL_BLOCK_ENTITY, pos, state);
    }
}
