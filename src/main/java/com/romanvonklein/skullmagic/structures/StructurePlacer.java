package com.romanvonklein.skullmagic.structures;

import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class StructurePlacer {

    public static StructureTemplate place(ServerWorld world, boolean bl, Identifier structureName, BlockPos blockPos) {
        Optional<StructureTemplate> optional;
        StructureTemplateManager structureManager = world.getStructureTemplateManager();
        StructureTemplate structure;
        try {
            optional = structureManager.getTemplate(structureName);
            structure = optional.get();
        } catch (InvalidIdentifierException invalidIdentifierException) {
            return null;
        }

        if (!bl) {
            StructurePlacementData structurePlacementData = new StructurePlacementData();
            // .setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities);

            structure.place(world, blockPos, blockPos, structurePlacementData,
                    Random.create(), Block.NOTIFY_LISTENERS);
            return null;
        }
        return structure;
    }
}
