package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class SpellPedestalBlockEntity extends BlockEntity {

    public String spellName;

    public SpellPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.SPELL_PEDESTAL_BLOCK_ENTITY, pos, state);
    }

}
