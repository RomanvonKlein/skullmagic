package com.romanvonklein.skullmagic.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;

public class SkullMagicSkullBlock extends SkullBlock {

    public SkullMagicSkullBlock(SkullMagicSkullType skullType, Settings settings) {
        super(skullType, settings);
        this.setDefaultState((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(ROTATION, 0));
    }

    public static enum SkullMagicSkullType implements SkullType {
        ENDERMAN,
        BLAZE,
        SPIDER;
    }

}
