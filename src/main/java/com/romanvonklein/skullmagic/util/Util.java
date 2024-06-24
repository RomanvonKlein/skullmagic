package com.romanvonklein.skullmagic.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.SkullPedestalBlockEntity;
import com.romanvonklein.skullmagic.config.Config;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.world.World;

public class Util {
    public static String getPedestalSkullIdentifier(World world, BlockPos pedestalPos) {
        Optional<SkullPedestalBlockEntity> pedestalCandidate = world.getBlockEntity(pedestalPos,
                SkullMagic.SKULL_PEDESTAL_BLOCK_ENTITY);
        String result = null;
        if (pedestalCandidate.isPresent()) {
            BlockState skullCandidate = world.getBlockState(pedestalPos.up());
            String blockIdentifier = Registries.BLOCK.getId(skullCandidate.getBlock()).toString();
            if (Config.getConfig().skulls.containsKey(blockIdentifier)) {
                result = blockIdentifier;
            }
        }
        return result;
    }

    public static <T extends BlockEntity> List<T> getBlockEntitiesOfTypeInBox(
            ServerWorld world,
            Box box, BlockEntityType<T> bet) {
        ArrayList<T> results = new ArrayList<>();
        for (double x = box.minX; x <= box.maxX; x++) {
            for (double y = box.minY; y <= box.maxY; y++) {
                for (double z = box.minZ; z <= box.maxZ; z++) {
                    BlockPos candidatePos = BlockPos.ofFloored(x, y, z);
                    Optional<T> opt = world.getBlockEntity(candidatePos, bet);
                    if (opt.isPresent()) {
                        results.add(opt.get());
                    }
                }
            }
        }
        return results;
    }

    public static boolean inRange(BlockPos altarPos, BlockPos pos, int rangeWidth, int rangeHeight) {
        BlockPos diff = altarPos.subtract(pos);
        return Math.abs(diff.getX()) <= rangeWidth && Math.abs(diff.getY()) <= rangeHeight
                && Math.abs(diff.getZ()) <= rangeWidth;
    }

    @Nullable
    public static SpawnerEntry getSpawnForSpawnerWithClass(String spawnClass) {
        Map<String, ArrayList<SpawnerEntry>> spawns = Config.getConfig().spawnerSpawns;
        SpawnerEntry result = null;
        if (spawns.containsKey(spawnClass) && !spawns.get(spawnClass).isEmpty()) {
            ArrayList<SpawnerEntry> entries = spawns.get(spawnClass);
            int totalWeights = 0;
            ArrayList<Integer> weights = new ArrayList<>();
            for (SpawnerEntry entry : entries) {
                weights.add(totalWeights);
                totalWeights += entry.weight;
            }
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            int chosenWeight = rand.nextInt(0, totalWeights);
            int index;
            for (index = 0; index < weights.size() && weights.get(index) < chosenWeight; index++) {
            }
            index = index > 0 ? index - 1 : 0;
            result = entries.get(index);
        }
        return result;
    }
}
