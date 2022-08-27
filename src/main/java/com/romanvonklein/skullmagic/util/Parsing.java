package com.romanvonklein.skullmagic.util;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.util.math.BlockPos;

public class Parsing {
    public static BlockPos shortStringToBlockPos(String shortString) {
        SkullMagic.LOGGER.error("THE BLOCKPOS(" + shortString + ") IS NOT YET PARSED CORRECTLY!!!");
        return new BlockPos(1, 1, 1);
    }
}
