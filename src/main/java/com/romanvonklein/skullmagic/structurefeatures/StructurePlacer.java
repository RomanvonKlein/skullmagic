package com.romanvonklein.skullmagic.structurefeatures;

import java.util.Optional;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;

public class StructurePlacer {

    public static Structure place(ServerWorld world, boolean bl, Identifier structureName, BlockPos blockPos) {
        Optional<Structure> optional;
        StructureManager structureManager = world.getStructureManager();
        Structure structure;
        try {
            optional = structureManager.getStructure(structureName);
            structure = optional.get();
        } catch (InvalidIdentifierException invalidIdentifierException) {
            return null;
        }

        if (!bl) {
            StructurePlacementData structurePlacementData = new StructurePlacementData();
            // .setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities);

            structure.place(world, blockPos, blockPos, structurePlacementData,
                    new Random(), Block.NOTIFY_LISTENERS);
            return null;
        }
        return structure;
    }
}
