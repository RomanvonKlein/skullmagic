package com.romanvonklein.skullmagic.structures;

import java.util.Optional;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class BigJigsawStructure extends Structure {

    public static final int MAX_SIZE = 128;
    public static final Codec<BigJigsawStructure> CODEC = RecordCodecBuilder
            .mapCodec((RecordCodecBuilder.Instance<BigJigsawStructure> instance) -> {
                return instance.group(configCodecBuilder(instance),
                        StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter((BigJigsawStructure structure) -> {
                            return structure.startPool;
                        }), Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter((structure) -> {
                            return structure.startJigsawName;
                        }), Codec.intRange(0, 20).fieldOf("size").forGetter((structure) -> {
                            return structure.size;
                        }), HeightProvider.CODEC.fieldOf("start_height").forGetter((structure) -> {
                            return structure.startHeight;
                        }), Codec.BOOL.fieldOf("use_expansion_hack").forGetter((structure) -> {
                            return structure.useExpansionHack;
                        }), Type.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter((structure) -> {
                            return structure.projectStartToHeightmap;
                        }), Codec.intRange(1, 256).fieldOf("max_distance_from_center").forGetter((structure) -> {
                            return structure.maxDistanceFromCenter;
                        })).apply(instance, BigJigsawStructure::new);
            }).flatXmap(createValidator(), createValidator()).codec();

    private final RegistryEntry<StructurePool> startPool;
    private final Optional<Identifier> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Type> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    private static Function<BigJigsawStructure, DataResult<BigJigsawStructure>> createValidator() {
        return (feature) -> {
            byte heightOnTerrain;
            switch (feature.getTerrainAdaptation()) {
                case NONE:
                    heightOnTerrain = 0;
                    break;
                case BURY:
                case BEARD_THIN:
                case BEARD_BOX:
                    heightOnTerrain = 12;
                    break;
                default:
                    throw new IncompatibleClassChangeError();
            }

            int i = heightOnTerrain;
            return feature.maxDistanceFromCenter + i > 256
                    ? DataResult.error("Structure size including terrain adaptation must not exceed 256")
                    : DataResult.success(feature);
        };
    }

    public BigJigsawStructure(Structure.Config config, RegistryEntry<StructurePool> startPool,
            Optional<Identifier> startJigsawName, int size, HeightProvider startHeight, boolean useExpansionHack,
            Optional<Heightmap.Type> projectStartToHeightmap, int maxDistanceFromCenter) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    public BigJigsawStructure(Structure.Config config, RegistryEntry<StructurePool> startPool, int size,
            HeightProvider startHeight, boolean useExpansionHack, Heightmap.Type projectStartToHeightmap) {
        this(config, startPool, Optional.empty(), size, startHeight, useExpansionHack,
                Optional.of(projectStartToHeightmap), 80);
    }

    public BigJigsawStructure(Structure.Config config, RegistryEntry<StructurePool> startPool, int size,
            HeightProvider startHeight, boolean useExpansionHack) {
        this(config, startPool, Optional.empty(), size, startHeight, useExpansionHack, Optional.empty(), 80);
    }

    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        ChunkPos chunkPos = context.chunkPos();
        int i = this.startHeight.get(context.random(), new HeightContext(context.chunkGenerator(), context.world()));
        BlockPos blockPos = new BlockPos(chunkPos.getStartX(), i, chunkPos.getStartZ());
        return StructurePoolBasedGenerator.generate(context, this.startPool, this.startJigsawName, this.size, blockPos,
                this.useExpansionHack, this.projectStartToHeightmap, this.maxDistanceFromCenter);
    }

    public StructureType<?> getType() {
        return StructureType.JIGSAW;
    }

}
