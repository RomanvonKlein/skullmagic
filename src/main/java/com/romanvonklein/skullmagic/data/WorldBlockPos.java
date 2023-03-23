package com.romanvonklein.skullmagic.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class WorldBlockPos extends BlockPos {
    public RegistryKey<World> worldKey;

    public WorldBlockPos(BlockPos blockPos, RegistryKey<World> worldKey) {
        super(blockPos);
        this.worldKey = worldKey;
    }

    @Override
    public boolean equals(Object pos2Candidate) {
        if (pos2Candidate instanceof WorldBlockPos) {
            WorldBlockPos pos2 = (WorldBlockPos) pos2Candidate;
            return pos2.worldKey.toString().equals(this.worldKey.toString()) && pos2.getX() == this.getX()
                    && pos2.getY() == this.getY() && pos2.getZ() == this.getZ();
        }
        return false;
    }
}
