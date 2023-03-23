package com.romanvonklein.skullmagic.util;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.SkullPedestalBlockEntity;
import com.romanvonklein.skullmagic.config.Config;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class Util {
    public static boolean isValidSkullPedestalCombo(World world, BlockPos pedestalPos) {
        java.util.Optional<SkullPedestalBlockEntity> pedestalCandidate = world.getBlockEntity(pedestalPos,
                SkullMagic.SKULL_PEDESTAL_BLOCK_ENTITY);
        if (pedestalCandidate.isPresent()) {
            BlockState skullCandidate = world.getBlockState(pedestalPos.up());
            String blockIdentifier = Registry.BLOCK.getId(skullCandidate.getBlock()).toString();
            return Config.getConfig().skulls.containsKey(blockIdentifier);
        }
        return false;
    }
}
