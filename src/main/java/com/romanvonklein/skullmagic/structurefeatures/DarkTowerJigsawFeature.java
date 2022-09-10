package com.romanvonklein.skullmagic.structurefeatures;

import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;

import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.structure.StructureSetKeys;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import net.minecraft.world.gen.random.AtomicSimpleRandom;
import net.minecraft.world.gen.random.ChunkRandom;

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

    /**
     * @param context
     * @return
     */
    private static boolean canGenerate(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context) {
        // ChunkPos chunkPos = context.chunkPos();
        // int i = chunkPos.x >> 4;
        // int j = chunkPos.z >> 4;
        // ChunkRandom chunkRandom = new ChunkRandom(new AtomicSimpleRandom(0L));
        // chunkRandom.setSeed((long) (i ^ j << 4) ^ context.seed());
        // chunkRandom.nextInt();
        // if (chunkRandom.nextInt(5) != 0) {
        //     return false;
        // }
        // return !context.chunkGenerator().method_41053(StructureSetKeys.VILLAGES,
        //         context.seed(), chunkPos.x, chunkPos.z,
        //         10);
        return true;
    }

    @Override
    public GenerationStep.Feature getGenerationStep() {
        return GenerationStep.Feature.SURFACE_STRUCTURES;
    }

}