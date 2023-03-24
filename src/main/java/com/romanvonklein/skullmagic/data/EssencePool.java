package com.romanvonklein.skullmagic.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.util.Parsing;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

class EssencePool extends PersistentState {
    private BlockPos altarPos;
    private RegistryKey<World> worldKey;
    private HashMap<BlockPos, String> pedestals;
    private ArrayList<BlockPos> consumers;
    private int essence;

    public EssencePool() {
        altarPos = null;
        worldKey = null;
        pedestals = new HashMap<>();
        consumers = new ArrayList<>();
        essence = 0;
    }

    int getEssence() {
        return essence;
    }

    void setEssence(int essence, UUID playerToUpdate) {
        this.essence = essence;
    }

    private int essenceChargeRate;

    int getEssenceChargeRate() {
        return essenceChargeRate;
    }

    void setEssenceChargeRate(int currentChargeRate, UUID playerToUpdate) {
        this.essenceChargeRate = currentChargeRate;
    }

    private int maxEssence;

    int getMaxEssence() {
        return maxEssence;
    }

    void setMaxEssence(int maxEssence, UUID playerToUpdate) {
        this.maxEssence = maxEssence;
    }

    EssencePool(BlockPos altarPos, RegistryKey<World> worldKey, HashMap<BlockPos, String> pedestals,
            ArrayList<BlockPos> consumers) {
        this.altarPos = altarPos;
        this.worldKey = worldKey;
        this.pedestals = pedestals;
        this.consumers = consumers;

    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {

        // altarPos
        if (altarPos != null) {
            tag.putIntArray("altarPos", new int[] { altarPos.getX(), altarPos.getY(), altarPos.getZ() });
        }

        // worldkey
        if (worldKey != null) {
            tag.putString("worldKey", worldKey.getValue().toString());
        }
        // pedestals
        NbtCompound pedestalsNbt = new NbtCompound();
        pedestals.keySet().forEach(pedestalPos -> {
            pedestalsNbt.putString(pedestalPos.toShortString(), pedestals.getOrDefault(pedestalPos, ""));
        });
        tag.put("pedestals", pedestalsNbt);

        // consumers
        int[] allConsumerCoords = new int[consumers.size() * 3];
        for (int i = 0; i < consumers.size(); i++) {
            BlockPos pos = consumers.get(i);
            allConsumerCoords[i * 3] = pos.getX();
            allConsumerCoords[i * 3 + 1] = pos.getY();
            allConsumerCoords[i * 3 + 2] = pos.getZ();
        }
        tag.putIntArray("consumers", allConsumerCoords);
        return tag;
    }

    static EssencePool fromNbt(NbtCompound tag) {
        // altarPos
        BlockPos altarPos = null;
        if (tag.contains("altarPos")) {
            int[] altarCoords = tag.getIntArray("altarPos");
            altarPos = new BlockPos(altarCoords[0], altarCoords[0], altarCoords[0]);
        }

        // worldkey
        RegistryKey<World> worldKey = null;
        if (tag.contains("worldKey")) {
            String worldKeyString = tag.getString("worldKey");
            worldKey = RegistryKey.of(net.minecraft.util.registry.Registry.WORLD_KEY,
                    Identifier.tryParse(worldKeyString));
        }

        // pedestals
        HashMap<BlockPos, String> pedestals = new HashMap<>();
        NbtCompound pedestalCompound = tag.getCompound("pedestals");
        for (String key : pedestalCompound.getKeys()) {
            pedestals.put(Parsing.shortStringToBlockPos(key), pedestalCompound.getString(key));
        }

        // consumers
        ArrayList<BlockPos> consumers = new ArrayList<>();
        int[] consumerCoords = tag.getIntArray("consumers");
        for (int i = 0; i < consumerCoords.length; i += 3) {
            consumers.add(new BlockPos(consumerCoords[i], consumerCoords[i + 1], consumerCoords[i + 2]));
        }

        return new EssencePool(altarPos, worldKey, pedestals, consumers);

    }

    boolean dischargeEssence(int reducedEssenceCost, UUID playerToUpdate) {
        boolean success = false;
        if (this.essence >= reducedEssenceCost) {
            this.essence -= reducedEssenceCost;
            success = true;
        }
        return success;
    }

    void tick(MinecraftServer server, UUID playerToUpdate) {
        this.essence += this.essenceChargeRate;
        if (this.essence > this.maxEssence) {
            this.essence = this.maxEssence;
        }
        /*
         * consumer entities should tick by themselves...
         * for (BlockPos consumerPos : this.consumers) {
         * ServerWorld world = server.getWorld(this.worldKey);
         * world.getBlockEntity(consumerPos, AConsumerBlockEntity);
         * AConsumerBlockEntity consumerEntity = ;
         * consumerEntity.tick(server);
         * }
         */
    }

    public void addPedestal(BlockPos pedPos, String skullIdentifier, UUID playerToUpdate) {
        this.pedestals.put(pedPos, skullIdentifier);
        this.essenceChargeRate += Config.getConfig().skulls.get(skullIdentifier);
    }

    public void removePedestal(BlockPos pedPos, UUID playerToUpdate) {
        this.essenceChargeRate -= Config.getConfig().skulls.get(this.pedestals.get(pedPos));
        this.pedestals.remove(pedPos);
        SkullMagic.updatePlayer(playerToUpdate);
    }

    public BlockPos getAltarPos() {
        return this.altarPos;
    }

    public RegistryKey<World> getWorldKey() {
        return this.worldKey;
    }

    public Set<BlockPos> getPedestalPositions() {
        return this.pedestals.keySet();
    }

    public ArrayList<BlockPos> getConsumerPositions() {
        return this.consumers;
    }

}