package com.romanvonklein.skullmagic.structurefeatures;

import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;

import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.structure.EndCityStructure;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class DarkTowerJigsawFeature extends Structure {
    public static final Codec<DarkTowerJigsawFeature> CODEC = createCodec(DarkTowerJigsawFeature::new);
    public DarkTowerJigsawFeature(Structure.Config config) {
        super(config);
    }
    @Override
    protected Optional<StructurePosition> getStructurePosition(Context context) {
        return Optional.empty();
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.JIGSAW;
    }
}