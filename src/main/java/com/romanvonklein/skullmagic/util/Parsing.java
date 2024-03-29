package com.romanvonklein.skullmagic.util;

import java.util.Set;

import net.minecraft.util.math.BlockPos;

public class Parsing {
    public static BlockPos shortStringToBlockPos(String shortString) {
        String[] arr = shortString.split(", ");
        return new BlockPos(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
    }

    public static String[] setToStringArr(Set<String> set) {
        String[] arr = new String[set.size()];
        int counter = 0;
        for (String entry : set) {
            arr[counter] = entry;
            counter++;
        }
        return arr;
    }

    public static BlockPos stringToBlockpos(String posStr) {
        String[] parts = posStr.split(",");
        return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    public static String blockPosToString(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
