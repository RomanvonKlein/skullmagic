package com.romanvonklein.skullmagic.structurefeatures;

import com.mojang.serialization.Codec;
import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.JigsawFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class DarkTowerFeature extends JigsawFeature {

    public DarkTowerFeature(Codec<StructurePoolFeatureConfig> configCodec) {
        super(configCodec, 0, true, true, DarkTowerFeature::canGenerate);
    }

    @Override
    public Codec<ConfiguredStructureFeature<StructurePoolFeatureConfig, StructureFeature<StructurePoolFeatureConfig>>> getCodec() {
        return super.getCodec();
    }

    private static boolean canGenerate(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context) {
        // ChunkPos chunkPos = context.chunkPos();
        // int i = chunkPos.x >> 4;
        // int j = chunkPos.z >> 4;
        // ChunkRandom chunkRandom = new ChunkRandom(new AtomicSimpleRandom(0L));
        // chunkRandom.setSeed((long) (i ^ j << 4) ^ context.seed());
        // chunkRandom.nextInt();
        // if (chunkRandom.nextInt(5) != 0) {
        // return false;
        // }
        // return !context.chunkGenerator().method_41053(StructureSetKeys.VILLAGES,
        // context.seed(), chunkPos.x, chunkPos.z,
        // 10);
        SkullMagic.LOGGER.info("requested generation step");
        return true;
    }

    @Override
    public GenerationStep.Feature getGenerationStep() {
        return GenerationStep.Feature.SURFACE_STRUCTURES;
    }

}