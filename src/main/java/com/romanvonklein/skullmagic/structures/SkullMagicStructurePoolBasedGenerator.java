package com.romanvonklein.skullmagic.structures;

import java.util.List;

import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.mutable.MutableObject;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class SkullMagicStructurePoolBasedGenerator extends StructurePoolBasedGenerator {
/*
    public static Optional<StructurePiecesGenerator<FeatureConfig>> generateNew(
            StructureGeneratorFactory.Context<FeatureConfig> context2, PieceFactory pieceFactory,
            BlockPos pos, boolean modifyBoundingBox, boolean goBelowSpawnLevel) {
        ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(0L));
        chunkRandom.setCarverSeed(context2.seed(), context2.chunkPos().x, context2.chunkPos().z);
        DynamicRegistryManager dynamicRegistryManager = context2.registryManager();
        FeatureConfig structurePoolFeatureConfig = context2.config();
        ChunkGenerator chunkGenerator = context2.chunkGenerator();
        StructureTemplateManager structureManager = context2.structureTemplateManager();
        HeightLimitView heightLimitView = context2.world();
        Predicate<RegistryEntry<Biome>> predicate = context2.validBiome();
        Structure.init();
        Registry<StructurePool> registry = dynamicRegistryManager.get(RegistryKeys.STRUCTURE_POOL_ELEMENT);


        BlockRotation blockRotation = BlockRotation.random(chunkRandom);
        StructurePool structurePool = structurePoolFeatureConfig.getStartPool().value();
        StructurePoolElement structurePoolElement = structurePool.getRandomElement(chunkRandom);
        if (structurePoolElement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }
        PoolStructurePiece poolStructurePiece = pieceFactory.create(structureManager, structurePoolElement, pos,
                structurePoolElement.getGroundLevelDelta(), blockRotation,
                structurePoolElement.getBoundingBox(structureManager, pos, blockRotation));
        BlockBox blockBox = poolStructurePiece.getBoundingBox();
        int i = (blockBox.getMaxX() + blockBox.getMinX()) / 2;
        int j = (blockBox.getMaxZ() + blockBox.getMinZ()) / 2;
        int k = goBelowSpawnLevel
                ? pos.getY() + chunkGenerator.getHeightOnGround(i, j, Heightmap.Type.WORLD_SURFACE_WG, heightLimitView)
                : pos.getY();
        if (!predicate.test(chunkGenerator.getBiomeForNoiseGen(BiomeCoords.fromBlock(i), BiomeCoords.fromBlock(k),
                BiomeCoords.fromBlock(j)))) {
            return Optional.empty();
        }
        int l = blockBox.getMinY() + poolStructurePiece.getGroundLevelDelta();
        poolStructurePiece.translate(0, k - l, 0);
        return Optional.of((structurePiecesCollector, context) -> {
            ArrayList<PoolStructurePiece> list = Lists.newArrayList();
            list.add(poolStructurePiece);
            if (structurePoolFeatureConfig.getSize() <= 0) {
                return;
            }
            int boundaries = 240;
            Box box = new Box(i - boundaries, k - boundaries, j - boundaries, i + boundaries + 1, k + boundaries + 1,
                    j + boundaries + 1);
            StructurePoolGenerator structurePoolGenerator = new StructurePoolGenerator(registry,
                    structurePoolFeatureConfig.getSize(), pieceFactory, chunkGenerator, structureManager, list,
                    chunkRandom);
            structurePoolGenerator.structurePieces.addLast(new ShapedPoolStructurePiece(poolStructurePiece,
                    new MutableObject<VoxelShape>(VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box),
                            VoxelShapes.cuboid(Box.from(blockBox)), BooleanBiFunction.ONLY_FIRST)),
                    0));
            while (!structurePoolGenerator.structurePieces.isEmpty()) {
                ShapedPoolStructurePiece shapedPoolStructurePiece = structurePoolGenerator.structurePieces
                        .removeFirst();
                structurePoolGenerator.generatePiece(shapedPoolStructurePiece.piece,
                        shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.currentSize, modifyBoundingBox,
                        heightLimitView);
            }
            list.forEach(structurePiecesCollector::addPiece);
        });
        */



    public static void generate_1(DynamicRegistryManager registryManager, PoolStructurePiece piece, int maxDepth,
            PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureTemplateManager structureManager,
            List<? super PoolStructurePiece> results, Random random, HeightLimitView world) {
/*
        SkullMagic.LOGGER.info("Generating skullMagicStructure.");

        Registry<StructurePool> registry = registryManager.get(Registry.STRUCTURE_POOL_KEY);
        StructurePoolGenerator structurePoolGenerator = new StructurePoolGenerator(registry, maxDepth, pieceFactory,
                chunkGenerator, structureManager, results, random);

        BlockPos pos = piece.getPos();

        BlockBox blockBox = piece.getBoundingBox();
        int i = (blockBox.getMaxX() + blockBox.getMinX()) / 2;
        int j = (blockBox.getMaxZ() + blockBox.getMinZ()) / 2;
        int k = pos.getY();// + chunkGenerator.getHeightOnGround(i, j, Heightmap.Type.WORLD_SURFACE_WG,
                           // world);
        int l = blockBox.getMinY() + piece.getGroundLevelDelta();
        piece.translate(0, k - l, 0);

        int boundaries = 240;

        Box box = new Box(i - boundaries, k - boundaries, j - boundaries, i + boundaries + 1, k + boundaries + 1,
                j + boundaries + 1);
        ShapedPoolStructurePiece tempPiece = new ShapedPoolStructurePiece(piece,
                new MutableObject<VoxelShape>(VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box),
                        VoxelShapes.cuboid(Box.from(blockBox)), BooleanBiFunction.ONLY_FIRST)),
                0);
        structurePoolGenerator.structurePieces.addLast(tempPiece);
        structurePoolGenerator.children.add(tempPiece.piece);
        structurePoolGenerator.generatePiece(tempPiece.piece, tempPiece.pieceShape, 0, true, world);
        while (!structurePoolGenerator.structurePieces.isEmpty()) {
            ShapedPoolStructurePiece shapedPoolStructurePiece = structurePoolGenerator.structurePieces
                    .removeFirst();
            structurePoolGenerator.generatePiece(shapedPoolStructurePiece.piece,
                    shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.currentSize, true, world);
        }
        /*
         * structurePoolGenerator.structurePieces
         * .addLast(new ShapedPoolStructurePiece(piece, new
         * MutableObject<VoxelShape>(VoxelShapes.UNBOUNDED), 0));
         * while (!structurePoolGenerator.structurePieces.isEmpty()) {
         * ShapedPoolStructurePiece shapedPoolStructurePiece =
         * structurePoolGenerator.structurePieces.removeFirst();
         * structurePoolGenerator.generatePiece(shapedPoolStructurePiece.piece,
         * shapedPoolStructurePiece.pieceShape,
         * shapedPoolStructurePiece.currentSize, false, world);
         * }
         */

    }

    public static interface PieceFactory {
        public PoolStructurePiece create(StructureTemplateManager var1, StructurePoolElement var2, BlockPos var3, int var4,
                BlockRotation var5, BlockBox var6);
    }

    public static void generateFreely(ServerWorld world, int maxDepth, BlockPos pos, StructureTemplate structure) {
/*
        ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
        StructureTemplateManager structureManager = world.getStructureTemplateManager();
        StructureAccessor structureAccessor = world.getStructureAccessor();
        Random random = world.getRandom();
        ArrayList<PoolStructurePiece> list = Lists.newArrayList();
        // Structure structure = new Structure();
        // structure.saveFromWorld(world, pos, new Vec3i(1, 1, 1), false, null);
        SinglePoolElement structurePoolElement = new SinglePoolElement(structure);

        PoolStructurePiece poolStructurePiece = new PoolStructurePiece(structureManager, structurePoolElement, pos,
                0, BlockRotation.NONE,
                structurePoolElement.getBoundingBox(structureManager, pos, BlockRotation.NONE));
        SkullMagicStructurePoolBasedGenerator.generate_1(world.getRegistryManager(), poolStructurePiece,
                maxDepth,
                PoolStructurePiece::new, chunkGenerator, structureManager, list, random, world);
        for (PoolStructurePiece poolStructurePiece2 : list) {
            poolStructurePiece2.generate((StructureWorldAccess) world, structureAccessor, chunkGenerator, random,
                    BlockBox.infinite(), pos, false);
        }
        */
    }

    /*
     * public static void generateFreely(ServerWorld world, int size, BlockPos pos,
     * BlockRotation dir,
     * Identifier structurIdentifier) {
     * DynamicRegistryManager registryManager = world.getRegistryManager();
     * Registry<StructurePool> registry =
     * registryManager.get(Registry.STRUCTURE_POOL_KEY);
     * Deque<ShapedPoolStructurePiece> structurePieces = Queues.newArrayDeque();
     * Structure structure = new Structure();
     * structure.saveFromWorld(world, pos, new Vec3i(1, 1, 1), false, null);
     * 
     * SinglePoolElement structurePoolElement = new SinglePoolElement(structure);
     * PoolStructurePiece piece = new
     * PoolStructurePiece(world.getStructureManager(), structurePoolElement, pos, 1,
     * dir, new BlockBox(pos));
     * // structurePieces
     * // .addLast(new ShapedPoolStructurePiece(piece, new
     * // MutableObject<VoxelShape>(VoxelShapes.UNBOUNDED), 0));
     * structurePieces
     * .addLast(new ShapedPoolStructurePiece(piece, new
     * MutableObject<VoxelShape>(VoxelShapes.UNBOUNDED), 0));
     * Random random = new Random();
     * 
     * while (!structurePieces.isEmpty()) {
     * ShapedPoolStructurePiece shapedPoolStructurePiece =
     * structurePieces.removeFirst();
     * StructurePoolGenerator.generatePieceFreely(world, shapedPoolStructurePiece,
     * 1, size, false, registry,
     * structurePieces, random, PoolStructurePiece::new);
     * }
     * }
     */
    /*
    static final class StructurePoolGenerator {
        private final Registry<StructurePool> registry;
        private final int maxSize;
        private final PieceFactory pieceFactory;
        private final ChunkGenerator chunkGenerator;
        private final StructureTemplateManager structureManager;
        private final List<? super PoolStructurePiece> children;
        private final Random random;
        final Deque<ShapedPoolStructurePiece> structurePieces = Queues.newArrayDeque();

        StructurePoolGenerator(Registry<StructurePool> registry, int maxSize, PieceFactory pieceFactory,
                ChunkGenerator chunkGenerator, StructureTemplateManager structureManager,
                List<? super PoolStructurePiece> children, Random random) {
            this.registry = registry;
            this.maxSize = maxSize;
            this.pieceFactory = pieceFactory;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.children = children;
            this.random = random;
        }

        static void generatePieceFreely(ServerWorld world, ShapedPoolStructurePiece shapedPiece,
                                        int minY, int maxSize,
                                        boolean modifyBoundingBox, Registry<StructurePool> registry,
                                        Deque<ShapedPoolStructurePiece> structurePieces, Random random, PieceFactory pieceFactory) {
            /*
            StructureTemplateManager structureManager = world.getStructureTemplateManager();
            MutableObject<VoxelShape> pieceShape = shapedPiece.pieceShape;
            PoolStructurePiece piece = shapedPiece.piece;
            StructurePoolElement structurePoolElement = piece.getPoolElement();
            BlockPos blockPos = piece.getPos();
            BlockRotation blockRotation = piece.getRotation();
            StructurePool.Projection projection = structurePoolElement.getProjection();
            boolean bl = projection == StructurePool.Projection.RIGID;
            MutableObject<VoxelShape> mutableObject = new MutableObject<VoxelShape>();
            BlockBox blockBox = piece.getBoundingBox();
            int i = blockBox.getMinY();
            block0: for (StructureTemplate.StructureBlockInfo structureBlockInfo2 : structurePoolElement
                    .getStructureBlockInfos(structureManager, blockPos, blockRotation, random)) {
                StructurePoolElement structurePoolElement2;
                MutableObject<VoxelShape> mutableObject2;
                Direction direction = JigsawBlock.getFacing(structureBlockInfo2.state);
                BlockPos blockPos2 = structureBlockInfo2.pos;
                BlockPos blockPos3 = blockPos2.offset(direction);
                int j = blockPos2.getY() - i;
                int k = -1;
                Identifier identifier = new Identifier(structureBlockInfo2.nbt.getString("pool"));
                Optional<StructurePool> optional = registry.getOrEmpty(identifier);
                if (!optional.isPresent() || optional.get().getElementCount() == 0
                        && !Objects.equals(identifier, StructurePools.EMPTY.getValue())) {
                    SkullMagic.LOGGER.warn("Empty or non-existent pool: {}", (Object) identifier);
                    continue;
                }
                Identifier identifier2 = optional.get().getTerminatorsId();
                Optional<StructurePool> optional2 = registry.getOrEmpty(identifier2);
                if (!optional2.isPresent() || optional2.get().getElementCount() == 0
                        && !Objects.equals(identifier2, StructurePools.EMPTY.getValue())) {
                    SkullMagic.LOGGER.warn("Empty or non-existent fallback pool: {}", (Object) identifier2);
                    continue;
                }
                boolean bl2 = blockBox.contains(blockPos3);
                if (bl2) {
                    mutableObject2 = mutableObject;
                    if (mutableObject.getValue() == null) {
                        mutableObject.setValue(VoxelShapes.cuboid(Box.from(blockBox)));
                    }
                } else {
                    mutableObject2 = pieceShape;
                }
                ArrayList<StructurePoolElement> list = Lists.newArrayList();
                if (minY != maxSize) {
                    list.addAll(optional.get().getElementIndicesInRandomOrder(random));
                }
                list.addAll(optional2.get().getElementIndicesInRandomOrder(random));
                Iterator<StructurePoolElement> iterator = list.iterator();
                while (iterator.hasNext() && (structurePoolElement2 = (StructurePoolElement) iterator
                        .next()) != EmptyPoolElement.INSTANCE) {
                    for (BlockRotation blockRotation2 : BlockRotation.randomRotationOrder(random)) {
                        List<StructureTemplate.StructureBlockInfo> list2 = structurePoolElement2.getStructureBlockInfos(
                                structureManager, BlockPos.ORIGIN, blockRotation2, random);
                        BlockBox blockBox2 = structurePoolElement2.getBoundingBox(structureManager,
                                BlockPos.ORIGIN, blockRotation2);
                        int l = !modifyBoundingBox || blockBox2.getBlockCountY() > 16 ? 0
                                : list2.stream().mapToInt(structureBlockInfo -> {
                                    if (!blockBox2.contains(structureBlockInfo.pos
                                            .offset(JigsawBlock.getFacing(structureBlockInfo.state)))) {
                                        return 0;
                                    }
                                    Identifier old_identifier = new Identifier(
                                            structureBlockInfo.nbt.getString("pool"));
                                    Optional<StructurePool> old_optional = registry.getOrEmpty(old_identifier);
                                    Optional<Object> old_optional2 = old_optional
                                            .flatMap(pool -> registry.getOrEmpty(pool.getTerminatorsId()));
                                    int old_i = old_optional.map(pool -> pool.getHighestY(structureManager))
                                            .orElse(0);
                                    int old_j = old_optional2
                                            .map(pool -> ((StructurePool) pool).getHighestY(structureManager))
                                            .orElse(0);
                                    return Math.max(old_i, old_j);
                                }).max().orElse(0);
                        for (StructureTemplate.StructureBlockInfo structureBlockInfo22 : list2) {
                            int t;
                            int r;
                            int p;
                            if (!JigsawBlock.attachmentMatches(structureBlockInfo2, structureBlockInfo22))
                                continue;
                            BlockPos blockPos4 = structureBlockInfo22.pos;
                            BlockPos blockPos5 = blockPos3.subtract(blockPos4);
                            BlockBox blockBox3 = structurePoolElement2.getBoundingBox(structureManager, blockPos5,
                                    blockRotation2);
                            int m = blockBox3.getMinY();
                            StructurePool.Projection projection2 = structurePoolElement2.getProjection();
                            boolean bl3 = projection2 == StructurePool.Projection.RIGID;
                            int n = blockPos4.getY();
                            int o = j - n + JigsawBlock.getFacing(structureBlockInfo2.state).getOffsetY();
                            if (bl && bl3) {
                                p = i + o;
                            } else {
                                if (k == -1) {
                                    k = world.getTopPosition(Heightmap.Type.WORLD_SURFACE_WG, blockPos2).getY();
                                }
                                p = k - n;
                            }
                            int q = p - m;
                            BlockBox blockBox4 = blockBox3.offset(0, q, 0);
                            BlockPos blockPos6 = blockPos5.add(0, q, 0);
                            if (l > 0) {
                                r = Math.max(l + 1, blockBox4.getMaxY() - blockBox4.getMinY());
                                blockBox4.encompass(new BlockPos(blockBox4.getMinX(), blockBox4.getMinY() + r,
                                        blockBox4.getMinZ()));
                            }
                            if (VoxelShapes.matchesAnywhere((VoxelShape) mutableObject2.getValue(),
                                    VoxelShapes.cuboid(Box.from(blockBox4).contract(0.25)),
                                    BooleanBiFunction.ONLY_SECOND))
                                continue;
                            mutableObject2.setValue(VoxelShapes.combine((VoxelShape) mutableObject2.getValue(),
                                    VoxelShapes.cuboid(Box.from(blockBox4)), BooleanBiFunction.ONLY_FIRST));
                            r = piece.getGroundLevelDelta();
                            int s = bl3 ? r - o : structurePoolElement2.getGroundLevelDelta();
                            PoolStructurePiece poolStructurePiece = pieceFactory.create(structureManager,
                                    structurePoolElement2, blockPos6, s, blockRotation2, blockBox4);
                            if (bl) {
                                t = i + j;
                            } else if (bl3) {
                                t = p + n;
                            } else {
                                if (k == -1) {
                                    k = world.getTopPosition(Heightmap.Type.WORLD_SURFACE_WG, blockPos2).getY();
                                }
                                t = k + o / 2;
                            }
                            piece.addJunction(
                                    new JigsawJunction(blockPos3.getX(), t - j + r, blockPos3.getZ(), o, projection2));
                            poolStructurePiece.addJunction(
                                    new JigsawJunction(blockPos2.getX(), t - n + s, blockPos2.getZ(), -o, projection));
                            // children.add(poolStructurePiece);
                            if (minY + 1 > maxSize)
                                continue block0;
                            structurePieces.addLast(
                                    new ShapedPoolStructurePiece(poolStructurePiece, mutableObject2, minY + 1));
                            continue block0;
                        }
                    }
                }
            }

        }
*/
        void generatePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int minY,
                boolean modifyBoundingBox, HeightLimitView world) {
            /*
            StructurePoolElement structurePoolElement = piece.getPoolElement();
            BlockPos blockPos = piece.getPos();
            BlockRotation blockRotation = piece.getRotation();
            StructurePool.Projection projection = structurePoolElement.getProjection();
            boolean bl = projection == StructurePool.Projection.RIGID;
            MutableObject<VoxelShape> mutableObject = new MutableObject<VoxelShape>();
            BlockBox blockBox = piece.getBoundingBox();
            int i = blockBox.getMinY();
            block0: for (StructureTemplate.StructureBlockInfo structureBlockInfo2 : structurePoolElement
                    .getStructureBlockInfos(this.structureManager, blockPos, blockRotation, this.random)) {
                StructurePoolElement structurePoolElement2;
                MutableObject<VoxelShape> mutableObject2;
                Direction direction = JigsawBlock.getFacing(structureBlockInfo2.state);
                BlockPos blockPos2 = structureBlockInfo2.pos;
                BlockPos blockPos3 = blockPos2.offset(direction);
                int j = blockPos2.getY() - i;
                int k = -1;
                Identifier identifier = new Identifier(structureBlockInfo2.nbt.getString("pool"));
                Optional<StructurePool> optional = this.registry.getOrEmpty(identifier);
                if (!optional.isPresent() || optional.get().getElementCount() == 0
                        && !Objects.equals(identifier, StructurePools.EMPTY.getValue())) {
                    SkullMagic.LOGGER.warn("Empty or non-existent pool: {}", (Object) identifier);
                    continue;
                }
                Identifier identifier2 = optional.get().getTerminatorsId();
                Optional<StructurePool> optional2 = this.registry.getOrEmpty(identifier2);
                if (!optional2.isPresent() || optional2.get().getElementCount() == 0
                        && !Objects.equals(identifier2, StructurePools.EMPTY.getValue())) {
                    SkullMagic.LOGGER.warn("Empty or non-existent fallback pool: {}", (Object) identifier2);
                    continue;
                }
                boolean bl2 = blockBox.contains(blockPos3);
                if (bl2) {
                    mutableObject2 = mutableObject;
                    if (mutableObject.getValue() == null) {
                        mutableObject.setValue(VoxelShapes.cuboid(Box.from(blockBox)));
                    }
                } else {
                    mutableObject2 = pieceShape;
                }
                ArrayList<StructurePoolElement> list = Lists.newArrayList();
                if (minY != this.maxSize) {
                    list.addAll(optional.get().getElementIndicesInRandomOrder(this.random));
                }
                list.addAll(optional2.get().getElementIndicesInRandomOrder(this.random));
                Iterator<StructurePoolElement> iterator = list.iterator();
                while (iterator.hasNext() && (structurePoolElement2 = (StructurePoolElement) iterator
                        .next()) != EmptyPoolElement.INSTANCE) {
                    for (BlockRotation blockRotation2 : BlockRotation.randomRotationOrder(this.random)) {
                        List<StructureTemplate.StructureBlockInfo> list2 = structurePoolElement2.getStructureBlockInfos(
                                this.structureManager, BlockPos.ORIGIN, blockRotation2, this.random);
                        BlockBox blockBox2 = structurePoolElement2.getBoundingBox(this.structureManager,
                                BlockPos.ORIGIN, blockRotation2);
                        int l = !modifyBoundingBox || blockBox2.getBlockCountY() > 16 ? 0
                                : list2.stream().mapToInt(structureBlockInfo -> {
                                    if (!blockBox2.contains(structureBlockInfo.pos
                                            .offset(JigsawBlock.getFacing(structureBlockInfo.state)))) {
                                        return 0;
                                    }
                                    Identifier old_identifier = new Identifier(
                                            structureBlockInfo.nbt.getString("pool"));
                                    Optional<StructurePool> old_optional = this.registry.getOrEmpty(old_identifier);
                                    Optional<Object> old_optional2 = old_optional
                                            .flatMap(pool -> this.registry.getOrEmpty(pool.getTerminatorsId()));
                                    int old_i = old_optional.map(pool -> pool.getHighestY(this.structureManager))
                                            .orElse(0);
                                    int old_j = old_optional2
                                            .map(pool -> ((StructurePool) pool).getHighestY(this.structureManager))
                                            .orElse(0);
                                    return Math.max(old_i, old_j);
                                }).max().orElse(0);
                        for (StructureTemplate.StructureBlockInfo structureBlockInfo22 : list2) {
                            int t;
                            int r;
                            int p;
                            if (!JigsawBlock.attachmentMatches(structureBlockInfo2, structureBlockInfo22))
                                continue;
                            BlockPos blockPos4 = structureBlockInfo22.pos;
                            BlockPos blockPos5 = blockPos3.subtract(blockPos4);
                            BlockBox blockBox3 = structurePoolElement2.getBoundingBox(this.structureManager, blockPos5,
                                    blockRotation2);
                            int m = blockBox3.getMinY();
                            StructurePool.Projection projection2 = structurePoolElement2.getProjection();
                            boolean bl3 = projection2 == StructurePool.Projection.RIGID;
                            int n = blockPos4.getY();
                            int o = j - n + JigsawBlock.getFacing(structureBlockInfo2.state).getOffsetY();
                            if (bl && bl3) {
                                p = i + o;
                            } else {
                                if (k == -1) {
                                    k = this.chunkGenerator.getHeightOnGround(blockPos2.getX(), blockPos2.getZ(),
                                            Heightmap.Type.WORLD_SURFACE_WG, world);
                                }
                                p = k - n;
                            }
                            int q = p - m;
                            BlockBox blockBox4 = blockBox3.offset(0, q, 0);
                            BlockPos blockPos6 = blockPos5.add(0, q, 0);
                            if (l > 0) {
                                r = Math.max(l + 1, blockBox4.getMaxY() - blockBox4.getMinY());
                                blockBox4.encompass(new BlockPos(blockBox4.getMinX(), blockBox4.getMinY() + r,
                                        blockBox4.getMinZ()));
                            }
                            if (VoxelShapes.matchesAnywhere((VoxelShape) mutableObject2.getValue(),
                                    VoxelShapes.cuboid(Box.from(blockBox4).contract(0.25)),
                                    BooleanBiFunction.ONLY_SECOND))
                                continue;
                            mutableObject2.setValue(VoxelShapes.combine((VoxelShape) mutableObject2.getValue(),
                                    VoxelShapes.cuboid(Box.from(blockBox4)), BooleanBiFunction.ONLY_FIRST));
                            r = piece.getGroundLevelDelta();
                            int s = bl3 ? r - o : structurePoolElement2.getGroundLevelDelta();
                            PoolStructurePiece poolStructurePiece = this.pieceFactory.create(this.structureManager,
                                    structurePoolElement2, blockPos6, s, blockRotation2, blockBox4);
                            if (bl) {
                                t = i + j;
                            } else if (bl3) {
                                t = p + n;
                            } else {
                                if (k == -1) {
                                    k = this.chunkGenerator.getHeightOnGround(blockPos2.getX(), blockPos2.getZ(),
                                            Heightmap.Type.WORLD_SURFACE_WG, world);
                                }
                                t = k + o / 2;
                            }
                            piece.addJunction(
                                    new JigsawJunction(blockPos3.getX(), t - j + r, blockPos3.getZ(), o, projection2));
                            poolStructurePiece.addJunction(
                                    new JigsawJunction(blockPos2.getX(), t - n + s, blockPos2.getZ(), -o, projection));
                            this.children.add(poolStructurePiece);
                            if (minY + 1 > this.maxSize)
                                continue block0;
                            this.structurePieces.addLast(
                                    new ShapedPoolStructurePiece(poolStructurePiece, mutableObject2, minY + 1));
                            continue block0;
                        }
                    }
                }
            }
        }

             */
    }
/*
    static final class ShapedPoolStructurePiece {
        final PoolStructurePiece piece;
        final MutableObject<VoxelShape> pieceShape;
        final int currentSize;

        ShapedPoolStructurePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize) {
            this.piece = piece;
            this.pieceShape = pieceShape;
            this.currentSize = currentSize;
        }
    }
    */
}
