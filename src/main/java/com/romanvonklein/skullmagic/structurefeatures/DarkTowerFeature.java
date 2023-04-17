package com.romanvonklein.skullmagic.structurefeatures;

import com.mojang.serialization.Codec;

import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.world.gen.feature.FeatureConfig;

public class DarkTowerFeature extends DarkTowerJigsawFeature {
    public DarkTowerFeature(Codec<FeatureConfig> configCodec) {
        super(configCodec, 0, true, true, DarkTowerFeature::canGenerate);
    }

    private static boolean canGenerate(StructureGeneratorFactory.Context<FeatureConfig> structurePoolFeatureConfigContext) {
        return true;
    }

}
