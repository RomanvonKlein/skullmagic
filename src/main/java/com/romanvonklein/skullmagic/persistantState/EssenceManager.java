package com.romanvonklein.skullmagic.persistantState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.SkullPedestalBlockEntity;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.networking.NetworkingConstants;
import com.romanvonklein.skullmagic.util.Parsing;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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
    // TODO: is there something multiworld like? that goes above dimensions? if so,
    // anything static here should not be stati...
    private Map<RegistryKey<World>, Map<BlockPos, EssencePool>> EssencePools = new HashMap<>();

    private Map<RegistryKey<World>, Map<BlockPos, EssencePool>> pedestalsToEssencePools = new HashMap<>();
    private Map<UUID, EssencePool> playersToEssencePools = new HashMap<>();

    public EssencePool getEssencePool(RegistryKey<World> dimension, BlockPos pos) {
        if (!EssencePools.containsKey(dimension)) {
            EssencePools.put(dimension, new HashMap<>());
        }
        if (!EssencePools.get(dimension).containsKey(pos)) {
            EssencePools.get(dimension).put(pos, new EssencePool());
        }
        return EssencePools.get(dimension).get(pos);
    }

    public NbtCompound writeNbt(NbtCompound tag) {
        SkullMagic.LOGGER.info("Saving Mana pools:");
        NbtCompound EssencePoolsNBT = new NbtCompound();

        EssencePools.keySet().forEach(dimension -> {
            SkullMagic.LOGGER.info("Dimension: '" + dimension.getValue().toString() + "'");
            NbtCompound dimensionNBT = new NbtCompound();

            EssencePools.get(dimension).keySet().forEach((posStr) -> {
                NbtCompound EssencePoolNBT = new NbtCompound();
                EssencePool pool = EssencePools.get(dimension).get(posStr);
                pool.writeNbt(EssencePoolNBT);
                dimensionNBT.put(pool.position.toShortString(), EssencePoolNBT);
            });

            EssencePoolsNBT.put(dimension.getValue().toString(), dimensionNBT);
        });

        tag.put("essencePool", EssencePoolsNBT);
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
        System.out.println("Reading EssencePools from NBT");
        EssenceManager essenceMngr = new EssenceManager();
        essenceMngr.clear();
        if (tag.contains("essencePool")) {
            try {
                tag.getKeys().forEach((dimensionid) -> {
                    RegistryKey<World> key = RegistryKey.of(net.minecraft.util.registry.Registry.WORLD_KEY,
                            Identifier.tryParse(dimensionid));
                    if (!essenceMngr.EssencePools.containsKey(key)) {
                        essenceMngr.EssencePools.put(key, new HashMap<>());
                    }
                    tag.getCompound(dimensionid).getKeys().forEach((posString) -> {
                        EssencePool pool = EssencePool.fromNbt(tag.getCompound(posString));
                        if (pool.linkedPlayerID != null) {
                            essenceMngr.playersToEssencePools.put(pool.linkedPlayerID, pool);
                        }
                        for (BlockPos blockPos : pool.linkedPedestals.keySet()) {
                            essenceMngr.pedestalsToEssencePools.get(key).put(blockPos, pool);
                        }
                        essenceMngr.EssencePools.get(key).put(Parsing.shortStringToBlockPos(posString), pool);
                    });
                });
            } catch (Exception e) {
                SkullMagic.LOGGER.error("Failed loading persistentstate from NBT data!");
            }
        }
        return essenceMngr;
    }

    public void trySetLinkedPlayer(PlayerEntity player, BlockPos pos) {
        if (!player.world.isClient) {

            UUID playerID = player.getUuid();
            RegistryKey<World> key = player.getWorld().getRegistryKey();
            if (!(this.EssencePools.containsKey(key) && this.EssencePools.get(key).containsKey(pos))) {
                SkullMagic.LOGGER.warn("no valid mana pool was found for skullaltar. Creating one.");
                createNewEssencePool(player.getWorld(), pos);
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
                player.sendMessage(Text.of("Unlinked you from this altar."), true);
                ServerPlayNetworking.send((ServerPlayerEntity)(player), NetworkingConstants.UNLINK_ESSENCEPOOL_ID, null);
            } else {
                player.sendMessage(Text.of("This altar is already linked to another player."), true);
            }
        }
        /*
         * if (pool != null) {
         * pool.linkedPlayerID = null;
         * }
         * 
         * EssencePool prevpool = this.getEssencePoolForPlayer(player.getUuid());
         * 
         * if (SkullMagic.StateManager.playerHasLink(player.getUuid())
         * &&
         * !SkullMagic.StateManager.getLinkedAltarBlockPos(player.getUuid()).equals(this
         * .pos)) {
         * player.sendMessage(Text.of("You already have an altar linked to you at "
         * + SkullMagic.StateManager.getAltarPosLinkedToPlayer(player.getUuid()) + "."),
         * true);
         * } else {
         * String linkedUUID = SkullMagic.StateManager.getPlayerLinkedToAltar(this.pos);
         * if (linkedUUID.equals("")) {
         * this.linkedPlayerID = player.getUuidAsString();
         * SkullMagic.StateManager.addAltarLink(player.getUuid(), this.pos);
         * player.sendMessage(Text.of("Linked you to this altar."), true);
         * } else if (linkedUUID.equals(player.getUuidAsString())) {
         * this.linkedPlayerID = "";
         * SkullMagic.StateManager.removeAltarLink(player.getUuid(), this.pos);
         * player.sendMessage(Text.of("Unlinked you from this altar."), true);
         * } else {
         * 
         * }
         * }
         */
    }

    public void tick(MinecraftServer server) {
        for (Entry<RegistryKey<World>, Map<BlockPos, EssencePool>> dimensionSet : this.EssencePools.entrySet()) {
            World world = server.getWorld(dimensionSet.getKey());
            for (EssencePool pool : dimensionSet.getValue().values()) {
                pool.tick(world);
            }
        }
    }

    public void removeSkullAltar(RegistryKey<World> worldKey, BlockPos altarPos) {
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
            }
        }
    }

    public void removePedestal(RegistryKey<World> registryKey, BlockPos pos) {
        if (pedestalsToEssencePools.containsKey(registryKey)
                && pedestalsToEssencePools.get(registryKey).containsKey(pos)) {
            EssencePool pool = pedestalsToEssencePools.get(registryKey).get(pos);
            pool.removePedestal(pos);
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

    public void linkPedestalToEssencePool(RegistryKey<World> key, BlockPos pedestalPos, BlockPos altarPos,
            String skullIdentifier) {
        EssencePool pool = EssencePools.get(key).get(altarPos);
        linkPedestalToEssencePool(key, pedestalPos, pool, skullIdentifier);

    }

    public void linkPedestalToEssencePool(RegistryKey<World> key, BlockPos pedestalPos, EssencePool pool,
            String skullIdentifier) {
        pool.linkPedestal(pedestalPos, skullIdentifier);
        this.pedestalsToEssencePools.get(key).put(pedestalPos, pool);
    }

    public void createNewEssencePool(World world, BlockPos pos) {
        SkullMagic.LOGGER.info("creating new essencepool.");
        RegistryKey<World> registryKey = world.getRegistryKey();
        if (!EssencePools.containsKey(registryKey)) {
            EssencePools.put(registryKey, new HashMap<>());
        }
        EssencePool pool = new EssencePool();
        EssencePools.get(registryKey).put(pos, pool);

        tryLinkNearbyUnlinkedPedestals(world.getServer().getWorld(registryKey), pos);
    }

    public void tryLinkNearbyUnlinkedPedestals(World world, BlockPos altarPos) {
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
                    SkullMagic.essenceManager.linkPedestalToEssencePool(key, foundPedestalPos, altarPos,
                            skullCandidate);
                }
            }
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
}
