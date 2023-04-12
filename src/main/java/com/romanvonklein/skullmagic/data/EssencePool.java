package com.romanvonklein.skullmagic.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.config.Config.ConfigData;
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
    private ArrayList<BlockPos> capacityCrystals;
    private int essence;

    public EssencePool() {
        altarPos = null;
        worldKey = null;
        pedestals = new HashMap<>();
        consumers = new ArrayList<>();
        essence = 0;
        this.maxEssence = Config.getConfig().altarCapacity;
        this.essenceChargeRate = 0;
        this.capacityCrystals = new ArrayList<>();
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
        SkullMagic.updatePlayer(playerToUpdate);
    }

    private int maxEssence;

    int getMaxEssence() {
        return maxEssence;
    }

    void setMaxEssence(int maxEssence, UUID playerToUpdate) {
        this.maxEssence = maxEssence;
        SkullMagic.updatePlayer(playerToUpdate);
    }

    EssencePool(BlockPos altarPos, RegistryKey<World> worldKey, HashMap<BlockPos, String> pedestals,
            ArrayList<BlockPos> consumers, ArrayList<BlockPos> capacityCrystals, int essence) {
        this.altarPos = altarPos;
        this.worldKey = worldKey;
        this.pedestals = pedestals;
        this.consumers = consumers;
        this.capacityCrystals = capacityCrystals;
        this.maxEssence = Config.getConfig().altarCapacity;
        this.essence = essence;

        this.recalculateEssenceChargeRate();
        this.recalculateMaxEssence();
    }

    private void recalculateMaxEssence() {
        int essencePerCrystal = Config.getConfig().capacityCrystalStrength;
        this.maxEssence += essencePerCrystal * this.capacityCrystals.size();
    }

    private void recalculateEssenceChargeRate() {
        ConfigData config = Config.getConfig();
        for (String skullKey : this.pedestals.values()) {
            if (config.skulls.containsKey(skullKey)) {
                this.essenceChargeRate += config.skulls.get(skullKey);
            }
        }
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {

        // essence
        tag.putInt("essence", this.essence);

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

        // capacityCrystals
        int[] allCapacityCrystalCords = new int[this.capacityCrystals.size() * 3];
        for (int i = 0; i < capacityCrystals.size(); i++) {
            BlockPos pos = capacityCrystals.get(i);
            allCapacityCrystalCords[i * 3] = pos.getX();
            allCapacityCrystalCords[i * 3 + 1] = pos.getY();
            allCapacityCrystalCords[i * 3 + 2] = pos.getZ();
        }
        tag.putIntArray("capacityCrystals", allCapacityCrystalCords);
        return tag;
    }

    static EssencePool fromNbt(NbtCompound tag) {

        // altarPos
        BlockPos altarPos = null;
        if (tag.contains("altarPos")) {
            int[] altarCoords = tag.getIntArray("altarPos");
            altarPos = new BlockPos(altarCoords[0], altarCoords[1], altarCoords[2]);
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

        // capacity crystals
        ArrayList<BlockPos> capacityCrystals = new ArrayList<>();
        int[] capacityCrystalCoords = tag.getIntArray("capacityCrystals");
        for (int i = 0; i < capacityCrystalCoords.length; i += 3) {
            capacityCrystals
                    .add(new BlockPos(capacityCrystalCoords[i], capacityCrystalCoords[i + 1],
                            capacityCrystalCoords[i + 2]));
        }

        // essence
        int essence = tag.getInt("essence");

        EssencePool pool = new EssencePool(altarPos, worldKey, pedestals, consumers, capacityCrystals, essence);
        return pool;
    }

    boolean dischargeEssence(int reducedEssenceCost, UUID playerToUpdate) {
        boolean success = false;
        if (this.essence >= reducedEssenceCost) {
            this.essence -= reducedEssenceCost;
            success = true;
            SkullMagic.updatePlayer(playerToUpdate);
        }
        return success;
    }

    void tick(MinecraftServer server, UUID playerToUpdate) {
        int prevEssence = this.essence;
        this.essence += this.essenceChargeRate;
        if (this.essence > this.maxEssence) {
            this.essence = this.maxEssence;
        }
        if (prevEssence != this.essence) {
            SkullMagic.updatePlayer(playerToUpdate);
        }
    }

    public void addPedestal(BlockPos pedPos, String skullIdentifier, UUID playerToUpdate) {
        this.pedestals.put(pedPos, skullIdentifier);
        this.essenceChargeRate += Config.getConfig().skulls.get(skullIdentifier);
        SkullMagic.updatePlayer(playerToUpdate);
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

    public void clear() {
        this.altarPos = null;
        this.consumers.clear();
        this.essence = 0;
        this.essenceChargeRate = 0;
        this.maxEssence = 0;
        this.worldKey = null;
        this.pedestals.clear();
        this.capacityCrystals.clear();
    }

    public void addCapacityCrystal(BlockPos pos, UUID playerToUpdate) {
        this.capacityCrystals.add(pos);
        this.maxEssence += Config.getConfig().capacityCrystalStrength;
        SkullMagic.updatePlayer(playerToUpdate);
    }

    public ArrayList<BlockPos> getCapacityCrystalPositions() {
        return this.capacityCrystals;
    }

    public boolean containsCapacityCrystal(BlockPos pos) {
        boolean result = false;

        for (BlockPos candidate : this.capacityCrystals) {
            if (candidate.getX() == pos.getX() && candidate.getY() == pos.getY() && candidate.getZ() == pos.getZ()) {
                result = true;
                break;
            }
        }
        return result;
    }

    public void removeCapacityCrystal(BlockPos pos, UUID playerID) {
        BlockPos foundPos = null;
        for (BlockPos candidate : this.capacityCrystals) {
            if (candidate.getX() == pos.getX() && candidate.getY() == pos.getY() && candidate.getZ() == pos.getZ()) {
                foundPos = candidate;
                break;
            }
        }
        if (foundPos != null) {
            this.capacityCrystals.remove(foundPos);
            SkullMagic.updatePlayer(playerID);
        }
    }

    public void addConsumer(BlockPos pos, UUID playerID) {
        this.consumers.add(pos);
        SkullMagic.updatePlayer(playerID);
    }

    public boolean hasConumerAtPos(WorldBlockPos worldPos) {
        boolean result = false;
        for (BlockPos pos : this.consumers) {
            if (worldPos.isEqualTo(pos)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean tryRemoveConsumer(WorldBlockPos worldBlockPos, UUID playerToUpdate) {
        boolean result = false;
        BlockPos posToRemove = null;
        for (BlockPos pos : this.consumers) {
            if (worldBlockPos.isEqualTo(pos)) {
                posToRemove = pos;
                break;
            }
        }
        if (result) {
            this.consumers.remove(posToRemove);
            SkullMagic.updatePlayer(playerToUpdate);
        }
        return result;
    }

    public boolean canAfford(int essenceCost) {
        return this.getEssence() >= essenceCost;
    }

}