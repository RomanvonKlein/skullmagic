package com.romanvonklein.skullmagic.util;

import net.minecraft.util.math.BlockPos;

public class Parsing {
    public static BlockPos shortStringToBlockPos(String shortString) {
        String[] arr = shortString.split(", ");
        return new BlockPos(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
    }
}
