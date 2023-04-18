package com.romanvonklein.skullmagic.structures;

import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class DarkTowerStructure extends Structure {
    public DarkTowerStructure(Config configCodec) {
        super(configCodec);
    }

    @Override
    protected Optional<StructurePosition> getStructurePosition(Context context) {
        return Optional.empty();
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.JIGSAW;
    }

    private static boolean canGenerate(StructureGeneratorFactory.Context<FeatureConfig> structurePoolFeatureConfigContext) {
        return true;
    }

}
