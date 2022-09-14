package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blocks.AdvancedSpellShrine;
import com.romanvonklein.skullmagic.blocks.IntermediateSpellShrine;
import com.romanvonklein.skullmagic.blocks.SimpleSpellShrine;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class SpellShrineBlockEntity extends BlockEntity {
    public int level;

    public SpellShrineBlockEntity(BlockPos a, BlockState b, int level) {
        super(SkullMagic.SPELL_SHRINE_BLOCK_ENTITY, a, b);
        this.level = level;
    }

    public SpellShrineBlockEntity(BlockPos a, BlockState b) {
        super(SkullMagic.SPELL_SHRINE_BLOCK_ENTITY, a, b);
        this.level = 0;
        Block block = this.world.getBlockState(this.pos).getBlock();
        if (block instanceof SimpleSpellShrine) {
            this.level = 1;

        } else if (block instanceof IntermediateSpellShrine) {
            this.level = 2;

        } else if (block instanceof AdvancedSpellShrine) {
            this.level = 3;

        } else {
            SkullMagic.LOGGER.error("block for spellshrineblockentity does not seem to be valid spell shrine.");
        }
    }

}
