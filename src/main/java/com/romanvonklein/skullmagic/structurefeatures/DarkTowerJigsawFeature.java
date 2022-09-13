package com.romanvonklein.skullmagic.structurefeatures;

import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;

import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class DarkTowerJigsawFeature extends StructureFeature<StructurePoolFeatureConfig> {

    // public DarkTowerFeature(Codec<StructurePoolFeatureConfig> configCodec) {
    // super(configCodec, 0, true, true, DarkTowerFeature::canGenerate);
    // }

    public DarkTowerJigsawFeature(Codec<StructurePoolFeatureConfig> codec, int structureStartY,
            boolean modifyBoundingBox,
            boolean surface,
            Predicate<StructureGeneratorFactory.Context<StructurePoolFeatureConfig>> contextPredicate) {
        super(codec, context -> {
            if (!contextPredicate.test(context)) {
                return Optional.empty();
            }
            BlockPos blockPos = new BlockPos(context.chunkPos().getStartX(), structureStartY,
                    context.chunkPos().getStartZ());
            StructurePools.initDefaultPools();
            return SkullMagicStructurePoolBasedGenerator.generateNew(context, PoolStructurePiece::new, blockPos,
                    modifyBoundingBox,
                    surface);
        });
    }

    @Override
    public Codec<ConfiguredStructureFeature<StructurePoolFeatureConfig, StructureFeature<StructurePoolFeatureConfig>>> getCodec() {
        return super.getCodec();
    }

    @Override
    public GenerationStep.Feature getGenerationStep() {
        return GenerationStep.Feature.SURFACE_STRUCTURES;
    }

}