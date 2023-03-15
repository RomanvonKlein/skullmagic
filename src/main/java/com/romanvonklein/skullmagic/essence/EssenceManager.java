package com.romanvonklein.skullmagic.essence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.SkullPedestalBlockEntity;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.config.Config.ConfigData;
import com.romanvonklein.skullmagic.networking.NetworkingConstants;
import com.romanvonklein.skullmagic.networking.ServerPackageSender;
import com.romanvonklein.skullmagic.util.Parsing;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

/**
 * This class keeps track of all active mana-pools, updating essence levels,
 * charge rates and player linked to them.
 */
public class EssenceManager extends PersistentState {
    private Map<RegistryKey<World>, Map<BlockPos, EssencePool>> EssencePools = new HashMap<>();

    private Map<RegistryKey<World>, Map<BlockPos, EssencePool>> pedestalsToEssencePools = new HashMap<>();
    private Map<UUID, EssencePool> playersToEssencePools = new HashMap<>();
    private Map<RegistryKey<World>, Map<BlockPos, EssencePool>> consumersToEssencePools = new HashMap<>();
    private Map<RegistryKey<World>, ArrayList<BlockPos>> allConsumers = new HashMap<>();

    public EssenceManager() {
        super();
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    public EssencePool getEssencePool(RegistryKey<World> dimension, BlockPos pos, UUID playerID) {
        if (!EssencePools.containsKey(dimension)) {
            EssencePools.put(dimension, new HashMap<>());
        }
        if (!EssencePools.get(dimension).containsKey(pos)) {
            EssencePool pool = new EssencePool(pos, playerID);
            EssencePools.get(dimension).put(pos, pool);
            playersToEssencePools.put(playerID, pool);
        }
        return EssencePools.get(dimension).get(pos);
    }

    public NbtCompound writeNbt(NbtCompound tag) {
        // essence pools
        NbtCompound EssencePoolsNBT = new NbtCompound();
        EssencePools.keySet().forEach(dimension -> {
            NbtCompound dimensionNBT = new NbtCompound();

            EssencePools.get(dimension).keySet().forEach((posStr) -> {
                NbtCompound EssencePoolNBT = new NbtCompound();
                EssencePool pool = EssencePools.get(dimension).get(posStr);
                pool.writeNbt(EssencePoolNBT);
                dimensionNBT.put(pool.position.toShortString(), EssencePoolNBT);
            });

            EssencePoolsNBT.put(dimension.getValue().toString(), dimensionNBT);
        });
        tag.put("essencePools", EssencePoolsNBT);

        // consumers
        NbtCompound consumersNBT = new NbtCompound();
        allConsumers.keySet().forEach(worldKey -> {
            NbtCompound consumerListCompound = new NbtCompound();
            ArrayList<BlockPos> posList = this.allConsumers.get(worldKey);
            for (int index = 0; index < posList.size(); index++) {
                consumerListCompound.putString(Integer.toString(index), posList.get(index).toShortString());
            }
            consumersNBT.put(worldKey.getValue().toString(), consumerListCompound);
        });
        tag.put("consumers", consumersNBT);
        return tag;
    }

    public EssencePool getEssencePoolForPlayer(UUID playerID) {
        if (playersToEssencePools.containsKey(playerID)) {
            return playersToEssencePools.get(playerID);
        } else {
            return null;
        }
    }

    public EssencePool getEssencePoolForPedestal(RegistryKey<World> dimension, BlockPos pos) {
        if (EssencePools.containsKey(dimension) && EssencePools.get(dimension).containsKey(pos)) {
            return EssencePools.get(dimension).get(pos);
        }
        return null;
    }

    public void clear() {
        EssencePools = new HashMap<>();
        pedestalsToEssencePools = new HashMap<>();
        playersToEssencePools = new HashMap<>();
    }

    public static EssenceManager fromNbt(NbtCompound tag) {
        EssenceManager essenceMngr = new EssenceManager();
        // read all essencePools
        if (tag.contains("essencePools")) {
            NbtCompound essencePoolsCompound = tag.getCompound("essencePools");
            essencePoolsCompound.getKeys().forEach((worldKey) -> {
                RegistryKey<World> key = RegistryKey.of(net.minecraft.util.registry.Registry.WORLD_KEY,
                        Identifier.tryParse(worldKey));
                essenceMngr.EssencePools.put(key, new HashMap<>());
                NbtCompound worldList = essencePoolsCompound.getCompound(worldKey);
                worldList.getKeys().forEach((essencePoolPosString) -> {
                    EssencePool pool = EssencePool.fromNbt(worldList.getCompound(essencePoolPosString));
                    essenceMngr.EssencePools.get(key).put(Parsing.shortStringToBlockPos(essencePoolPosString),
                            pool);
                    if (pool.linkedPlayerID != null) {
                        essenceMngr.playersToEssencePools.put(pool.linkedPlayerID, pool);
                    }
                    for (BlockPos blockPos : pool.linkedPedestals.keySet()) {
                        if (!essenceMngr.pedestalsToEssencePools.containsKey(key)) {
                            essenceMngr.pedestalsToEssencePools.put(key, new HashMap<>());
                        }
                        essenceMngr.pedestalsToEssencePools.get(key).put(blockPos, pool);
                    }
                    essenceMngr.EssencePools.get(key).put(Parsing.shortStringToBlockPos(essencePoolPosString), pool);
                    for (BlockPos blockPos : pool.Consumers) {
                        if (!essenceMngr.consumersToEssencePools.containsKey(key)) {
                            essenceMngr.consumersToEssencePools.put(key, new HashMap<>());
                        }
                        essenceMngr.consumersToEssencePools.get(key).put(blockPos, pool);
                    }
                });
            });
        }
        // read all consumers
        if (tag.contains("consumers")) {
            NbtCompound consumersCompound = tag.getCompound("consumers");
            consumersCompound.getKeys().forEach((worldKey) -> {
                RegistryKey<World> key = RegistryKey.of(net.minecraft.util.registry.Registry.WORLD_KEY,
                        Identifier.tryParse(worldKey));
                essenceMngr.allConsumers.put(key, new ArrayList<>());
                NbtCompound consumerListCompound = consumersCompound.getCompound(worldKey);
                consumerListCompound.getKeys().forEach((indexStr) -> {
                    essenceMngr.allConsumers.get(key)
                            .add(Parsing.shortStringToBlockPos(consumerListCompound.getString(indexStr)));
                });
            });
        }

        return essenceMngr;
    }

    public String toJsonString() {
        JsonObject jobj = new JsonObject();

        SkullMagic.LOGGER.info("Creating essence Pools list");
        // essence pools
        JsonObject essencePools = new JsonObject();
        for (RegistryKey<World> key : this.EssencePools.keySet()) {
            JsonArray poolList = new JsonArray();
            for (BlockPos poolPos : this.EssencePools.get(key).keySet()) {
                EssencePool pool = this.EssencePools.get(key).get(poolPos);
                poolList.add(pool.toJsonElement());
            }
            essencePools.add(key.getValue().toString(), poolList);
        }
        jobj.add("essencePools", essencePools);

        // consumers
        SkullMagic.LOGGER.info("Creating consumers list");
        JsonObject consumers = new JsonObject();
        for (RegistryKey<World> key : this.allConsumers.keySet()) {
            JsonArray posList = new JsonArray();
            for (BlockPos pos : this.allConsumers.get(key)) {
                posList.add(pos.toShortString());
            }
            consumers.add(key.getValue().toString(), posList);
        }
        jobj.add("consumers", consumers);

        // playerToEssencePool
        SkullMagic.LOGGER.info("Creating playerToEssencePool");
        JsonObject playersToEssencePools = new JsonObject();
        for (Entry<UUID, EssencePool> entry : this.playersToEssencePools.entrySet()) {
            playersToEssencePools.addProperty(entry.getKey().toString(), entry.getValue().position.toShortString());
        }
        jobj.add("playersToEssencePools", playersToEssencePools);

        // pedestalToEssencePool
        SkullMagic.LOGGER.info("Creating pedestalToEssencePool");
        JsonObject pedestalToEssencePool = new JsonObject();
        for (RegistryKey<World> key : this.pedestalsToEssencePools.keySet()) {
            JsonArray pedestalList = new JsonArray();
            for (BlockPos poolPos : this.pedestalsToEssencePools.get(key).keySet()) {
                pedestalList.add(poolPos.toShortString());
            }
            pedestalToEssencePool.add(key.getValue().toString(), pedestalList);
        }
        jobj.add("pedestalToEssencePool", pedestalToEssencePool);

        // consumerToEssencePool
        SkullMagic.LOGGER.info("Creating consumerToEssencePool");
        JsonObject consumerToEssencePool = new JsonObject();
        for (RegistryKey<World> key : this.consumersToEssencePools.keySet()) {
            JsonArray consumerList = new JsonArray();
            for (BlockPos poolPos : this.consumersToEssencePools.get(key).keySet()) {
                consumerList.add(poolPos.toShortString());
            }
            consumerToEssencePool.add(key.getValue().toString(), consumerList);
        }
        jobj.add("consumerToEssencePool", consumerToEssencePool);

        return jobj.toString();
    }

    public void trySetLinkedPlayer(PlayerEntity player, BlockPos pos) {
        if (!player.world.isClient) {

            UUID playerID = player.getGameProfile().getId();
            RegistryKey<World> key = player.getWorld().getRegistryKey();
            if (!(this.EssencePools.containsKey(key) && this.EssencePools.get(key).containsKey(pos))) {
                SkullMagic.LOGGER.debug("no valid mana pool was found for skullaltar. Creating one.");
                createNewEssencePool(player.getWorld(), pos, playerID);
            }

            EssencePool pool = this.EssencePools.get(key).get(pos);
            if (pool.linkedPlayerID == null) {
                pool.linkedPlayerID = playerID;

                // unlink from old essence pool if there was one
                if (this.getEssencePoolForPlayer(playerID) != null) {
                    this.getEssencePoolForPlayer(playerID).linkedPlayerID = null;
                }

                // set links to new essence pool
                this.playersToEssencePools.remove(playerID);
                this.playersToEssencePools.put(playerID, pool);
                player.sendMessage(Text.of("Linked you to this altar."), true);
            } else if (pool.linkedPlayerID == playerID) {
                pool.linkedPlayerID = null;
                this.playersToEssencePools.remove(playerID);
                ServerPackageSender.sendUpdateLinksPackage((ServerPlayerEntity)player);
                ServerPackageSender.sendUpdateSpellListPackage((ServerPlayerEntity)player);
                player.sendMessage(Text.of("Unlinked you from this altar."), true);

                ServerPlayNetworking.send((ServerPlayerEntity) (player), NetworkingConstants.UNLINK_ESSENCEPOOL_ID,
                        PacketByteBufs.create());
            } else {
                player.sendMessage(Text.of("This altar is already linked to another player."), true);
            }
        }
    }

    public void tick(MinecraftServer server) {
        for (Entry<RegistryKey<World>, Map<BlockPos, EssencePool>> dimensionSet : this.EssencePools.entrySet()) {
            World world = server.getWorld(dimensionSet.getKey());
            for (EssencePool pool : dimensionSet.getValue().values()) {
                pool.tick(world);
            }
        }
    }

    public void removeSkullAltar(World world, BlockPos altarPos) {
        RegistryKey<World> worldKey = world.getRegistryKey();
        if (this.EssencePools.containsKey(worldKey) && this.EssencePools.get(worldKey).containsKey(altarPos)) {
            EssencePool poolToRemove = this.EssencePools.get(worldKey).get(altarPos);
            this.EssencePools.get(worldKey).remove(altarPos);

            // remove pedestal links
            ArrayList<BlockPos> pedestalsToRemove = new ArrayList<>();
            this.pedestalsToEssencePools.keySet().forEach((worldID) -> {
                this.pedestalsToEssencePools.get(worldID).forEach(
                        (pos, pool) -> {
                            if (pool == poolToRemove) {
                                pedestalsToRemove.add(pos);
                            }
                        });
            });
            for (BlockPos pos : pedestalsToRemove) {
                this.pedestalsToEssencePools.get(worldKey).remove(pos);
            }

            // remove player links
            ArrayList<UUID> playersToRemove = new ArrayList<>();
            this.playersToEssencePools.forEach((playerID, pool) -> {
                if (pool == poolToRemove) {
                    playersToRemove.add(playerID);
                }
            });
            for (UUID playerID : playersToRemove) {
                playersToEssencePools.remove(playerID);
                ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(playerID);
                if (player != null) {
                    ServerPlayNetworking.send(player, NetworkingConstants.UNLINK_ESSENCEPOOL_ID,
                            PacketByteBufs.create());
                }
            }
        }
    }

    public void removePedestal(ServerWorld world, BlockPos pos) {
        RegistryKey<World> registryKey = world.getRegistryKey();
        if (pedestalsToEssencePools.containsKey(registryKey)
                && pedestalsToEssencePools.get(registryKey).containsKey(pos)) {
            EssencePool pool = pedestalsToEssencePools.get(registryKey).get(pos);
            pool.removePedestal(world, pos);
            pedestalsToEssencePools.get(registryKey).remove(pos);

        }
    }

    public boolean playerHasEssencePool(UUID playerID) {
        return this.playersToEssencePools.containsKey(playerID);
    }

    public boolean pedestalIsLinked(RegistryKey<World> key, BlockPos foundPedestalPos) {

        return this.pedestalsToEssencePools.containsKey(key)
                && this.pedestalsToEssencePools.get(key).containsKey(foundPedestalPos);
    }

    public void linkPedestalToEssencePool(ServerWorld world, BlockPos pedestalPos, BlockPos altarPos,
            String skullIdentifier) {
        EssencePool pool = EssencePools.get(world.getRegistryKey()).get(altarPos);
        linkPedestalToEssencePool(world, pedestalPos, pool, skullIdentifier);

    }

    public void linkPedestalToEssencePool(ServerWorld world, BlockPos pedestalPos, EssencePool pool,
            String skullIdentifier) {
        RegistryKey<World> key = world.getRegistryKey();
        pool.linkPedestal(world, pedestalPos, skullIdentifier);
        if (!this.pedestalsToEssencePools.containsKey(key)) {
            this.pedestalsToEssencePools.put(key, new HashMap<>());
        }
        this.pedestalsToEssencePools.get(key).put(pedestalPos, pool);
    }

    public void createNewEssencePool(World world, BlockPos pos, UUID playerID) {
        SkullMagic.LOGGER.info("creating new essencepool.");
        RegistryKey<World> registryKey = world.getRegistryKey();
        if (!EssencePools.containsKey(registryKey)) {
            EssencePools.put(registryKey, new HashMap<>());
        }
        EssencePool pool = new EssencePool(pos, playerID);
        playersToEssencePools.put(playerID, pool);
        EssencePools.get(registryKey).put(pos, pool);

        tryLinkNearbyUnlinkedPedestals(world.getServer().getWorld(registryKey), pos, playerID);
    }

    public void tryLinkNearbyUnlinkedPedestals(ServerWorld world, BlockPos altarPos, UUID playerID) {
        int height = Config.getConfig().scanHeight;
        int width = Config.getConfig().scanWidth;
        ArrayList<BlockPos> foundPedestals = new ArrayList<>();
        for (int x = altarPos.getX() - width; x < altarPos.getX() + width; x++) {
            for (int y = altarPos.getY() - height; y < altarPos.getY() + height; y++) {
                for (int z = altarPos.getZ() - width; z < altarPos.getZ() + width; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isValidSkullPedestalCombo(world, pos)) {
                        foundPedestals.add(pos);
                    }
                }
            }
        }
        for (BlockPos foundPedestalPos : foundPedestals) {
            RegistryKey<World> key = world.getRegistryKey();
            if (!SkullMagic.essenceManager.pedestalIsLinked(key, foundPedestalPos)) {
                String skullCandidate = Registry.BLOCK.getId(world.getBlockState(foundPedestalPos.up()).getBlock())
                        .toString();
                if (Config.getConfig().skulls.containsKey(skullCandidate)) {
                    SkullMagic.essenceManager.linkPedestalToEssencePool(world, foundPedestalPos, altarPos,
                            skullCandidate);
                }
            }
        }
        ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(playerID);
        if (player != null) {
            ServerPackageSender.sendUpdateLinksPackage(player);
        }
    }

    public static boolean isValidSkullPedestalCombo(World world, BlockPos pedestalPos) {
        java.util.Optional<SkullPedestalBlockEntity> pedestalCandidate = world.getBlockEntity(pedestalPos,
                SkullMagic.SKULL_PEDESTAL_BLOCK_ENTITY);
        if (pedestalCandidate.isPresent()) {
            BlockState skullCandidate = world.getBlockState(pedestalPos.up());
            String blockIdentifier = Registry.BLOCK.getId(skullCandidate.getBlock()).toString();
            return Config.getConfig().skulls.containsKey(blockIdentifier);
        }
        return false;
    }

    public boolean hasEssencePoolAt(RegistryKey<World> key, BlockPos pos) {
        return this.EssencePools.containsKey(key) && this.EssencePools.get(key).containsKey(pos);
    }

    public void tryLinkSkullPedestalToNearbyAltar(ServerWorld world, BlockPos pedestalpos) {
        if (EssenceManager.isValidSkullPedestalCombo(world, pedestalpos)) {
            String skullCandidate = Registry.BLOCK
                    .getId(world.getBlockState(pedestalpos.up()).getBlock())
                    .toString();
            int height = Config.getConfig().scanHeight;
            int width = Config.getConfig().scanWidth;
            for (int x = pedestalpos.getX() - width; x <= pedestalpos.getX() + width; x++) {
                for (int y = pedestalpos.getY() - height; y <= pedestalpos.getY() + height; y++) {
                    for (int z = pedestalpos.getZ() - width; z <= pedestalpos.getZ() + width; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (SkullMagic.essenceManager.hasEssencePoolAt(world.getRegistryKey(), pos)) {
                            SkullMagic.essenceManager.linkPedestalToEssencePool(world, pedestalpos,
                                    pos,
                                    skullCandidate);
                            return;
                        }
                    }
                }
            }
        }
    }

    public EssencePool getEssencePoolForConsumer(RegistryKey<World> registryKey, BlockPos pos) {
        EssencePool pool = null;
        if (consumersToEssencePools.containsKey(registryKey)
                && consumersToEssencePools.get(registryKey).containsKey(pos)) {
            pool = consumersToEssencePools.get(registryKey).get(pos);
        }
        return pool;
    }

    public boolean addConsumer(RegistryKey<World> registryKey, BlockPos pos, UUID placerID) {
        boolean success = false;
        if (!this.allConsumers.containsKey(registryKey)) {
            this.allConsumers.put(registryKey, new ArrayList<>());
        }
        this.allConsumers.get(registryKey).add(pos);
        // see if the placer has an essencepool reaching the newly placed consumer
        if (this.playersToEssencePools.containsKey(placerID)) {
            EssencePool pool = this.playersToEssencePools.get(placerID);
            if (isConsumerInRangeOf(pos, pool.position)) {
                pool.addConsumer(pos);
                success = true;
                if (!this.consumersToEssencePools.containsKey(registryKey)) {
                    this.consumersToEssencePools.put(registryKey, new HashMap<>());
                }
                this.consumersToEssencePools.get(registryKey).put(pos, pool);
            }
        }
        return success;
    }

    private static boolean isConsumerInRangeOf(BlockPos consumerPos, BlockPos altarPos) {
        ConfigData config = Config.getConfig();
        int px = consumerPos.getX();
        int py = consumerPos.getY();
        int pz = consumerPos.getZ();
        int ax = altarPos.getX();
        int ay = altarPos.getY();
        int az = altarPos.getZ();
        return px <= ax + config.supplyWidth
                && px >= ax - config.supplyWidth
                && py <= ay + config.supplyHeight
                && py >= ay - config.supplyHeight
                && pz <= az + config.supplyWidth
                && pz >= az - config.supplyWidth;
    }

    public void removeConsumer(RegistryKey<World> registryKey, BlockPos pos) {
        if (this.consumersToEssencePools.containsKey(registryKey)
                && this.consumersToEssencePools.get(registryKey).containsKey(pos)) {
            EssencePool pool = this.consumersToEssencePools.get(registryKey).get(pos);
            pool.removeConsumer(pos);
            this.consumersToEssencePools.get(registryKey).remove(pos);
        }
    }

    public void removeCapacityCrystal(RegistryKey<World> registryKey, BlockPos pos) {
        if (this.consumersToEssencePools.containsKey(registryKey)
                && this.consumersToEssencePools.get(registryKey).containsKey(pos)) {
            EssencePool pool = this.consumersToEssencePools.get(registryKey).get(pos);
            pool.reduceMaxEssence(Config.getConfig().capacityCrystalStrength);
        }
        this.removeConsumer(registryKey, pos);
    }

    public boolean tryAddCapacityCrystal(RegistryKey<World> registryKey, BlockPos pos, UUID placerID) {
        if (addConsumer(registryKey, pos, placerID)) {
            this.consumersToEssencePools.get(registryKey).get(pos).maxEssence += Config
                    .getConfig().capacityCrystalStrength;
            return true;
        }
        return false;
    }
}