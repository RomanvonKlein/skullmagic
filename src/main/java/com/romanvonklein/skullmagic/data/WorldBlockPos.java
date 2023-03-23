package com.romanvonklein.skullmagic.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class WorldBlockPos extends BlockPos{
    public RegistryKey<World> worldKey;

    public WorldBlockPos(BlockPos blockPos, RegistryKey<World> worldKey) {
        super(blockPos);
        this.worldKey = worldKey;
    }
}
