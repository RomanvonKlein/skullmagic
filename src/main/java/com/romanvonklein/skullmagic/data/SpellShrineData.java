package com.romanvonklein.skullmagic.data;

import java.util.HashMap;

import com.romanvonklein.skullmagic.util.Parsing;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

class SpellShrineData extends PersistentState {
    BlockPos shrinePos;
    RegistryKey<World> worldKey;
    HashMap<BlockPos, Integer> powerPedestals;
    HashMap<BlockPos, Integer> efficiencyPedestals;
    HashMap<BlockPos, Integer> cooldownPedestals;

    public SpellShrineData() {
        this.powerPedestals = new HashMap<>();
        this.efficiencyPedestals = new HashMap<>();
        this.cooldownPedestals = new HashMap<>();
    }

    SpellShrineData(RegistryKey<World> worldKey, BlockPos shrinePos, HashMap<BlockPos, Integer> powerPedestals,
            HashMap<BlockPos, Integer> efficiencyPedestals, HashMap<BlockPos, Integer> cooldownPedestals) {
        this.worldKey = worldKey;
        this.shrinePos = shrinePos;
        this.powerPedestals = powerPedestals;
        this.efficiencyPedestals = efficiencyPedestals;
        this.cooldownPedestals = cooldownPedestals;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        // shrinePos
        if (this.shrinePos != null) {
            tag.putIntArray("shrinePos", new int[] { shrinePos.getX(), shrinePos.getY(), shrinePos.getZ() });
        }

        // worldkey
        if (this.worldKey != null) {
            tag.putString("worldKey", worldKey.getValue().toString());
        }

        // powerPedestals
        NbtCompound powerPedestalsCompound = new NbtCompound();
        for (BlockPos pedestalPos : powerPedestals.keySet()) {
            powerPedestalsCompound.putInt(pedestalPos.toShortString(), powerPedestals.get(pedestalPos));
        }
        tag.put("powerPedestals", powerPedestalsCompound);

        // efficiencyPedestals
        NbtCompound efficiencyPedestalsCompound = new NbtCompound();
        for (BlockPos pedestalPos : efficiencyPedestals.keySet()) {
            efficiencyPedestalsCompound.putInt(pedestalPos.toShortString(), efficiencyPedestals.get(pedestalPos));
        }
        tag.put("efficiencyPedestals", efficiencyPedestalsCompound);

        // powerPedestals
        NbtCompound cooldownPedestalsCompound = new NbtCompound();
        for (BlockPos pedestalPos : cooldownPedestals.keySet()) {
            cooldownPedestalsCompound.putInt(pedestalPos.toShortString(), cooldownPedestals.get(pedestalPos));
        }
        tag.put("cooldownPedestals", cooldownPedestalsCompound);

        return tag;
    }

    static SpellShrineData fromNbt(NbtCompound tag) {
        // shrinePos
        BlockPos shrinePos = null;
        if (tag.contains("shrinePos")) {
            int[] shrineCoords = tag.getIntArray("shrinePos");
            shrinePos = new BlockPos(shrineCoords[0], shrineCoords[1], shrineCoords[2]);
        }
        
        // worldkey
        RegistryKey<World> worldKey = null;
        if (tag.contains("worldKey")) {
            String worldKeyString = tag.getString("worldKey");
            worldKey = RegistryKey.of(Registry.WORLD_KEY,
                    Identifier.tryParse(worldKeyString));
        }

        // powerPedestals
        HashMap<BlockPos, Integer> powerPedestals = new HashMap<>();
        NbtCompound powerPedestalsCompound = tag.getCompound("powerPedestals");
        for (String pedestalPosString : powerPedestalsCompound.getKeys()) {
            powerPedestals.put(Parsing.shortStringToBlockPos(pedestalPosString),
                    powerPedestalsCompound.getInt(pedestalPosString));
        }

        // efficiencyPedestals
        HashMap<BlockPos, Integer> efficiencyPedestals = new HashMap<>();
        NbtCompound efficiencyPedestalsCompound = tag.getCompound("efficiencyPedestals");
        for (String pedestalPosString : efficiencyPedestalsCompound.getKeys()) {
            efficiencyPedestals.put(Parsing.shortStringToBlockPos(pedestalPosString),
                    efficiencyPedestalsCompound.getInt(pedestalPosString));
        }

        // cooldownPedestals
        HashMap<BlockPos, Integer> cooldownPedestals = new HashMap<>();
        NbtCompound cooldownPedestalsCompound = tag.getCompound("cooldownPedestals");
        for (String pedestalPosString : cooldownPedestalsCompound.getKeys()) {
            cooldownPedestals.put(Parsing.shortStringToBlockPos(pedestalPosString),
                    cooldownPedestalsCompound.getInt(pedestalPosString));
        }

        return new SpellShrineData(worldKey, shrinePos, powerPedestals, efficiencyPedestals, cooldownPedestals);
    }

    public double getPowerLevel() {
        double sum = 0;
        for (int powervalue : this.powerPedestals.values()) {
            sum += powervalue;
        }
        return 1.0 + Math.sqrt(sum);
    }

    public double getEfficiencyLevel() {
        double sum = 0;
        for (int efficiencyvalue : this.efficiencyPedestals.values()) {
            sum += efficiencyvalue;
        }
        return 1.0 + Math.sqrt(sum);
    }

    public double getCooldownLevel() {
        double sum = 0;
        for (int cooldownvalue : this.cooldownPedestals.values()) {
            sum += cooldownvalue;
        }
        return 1.0 + Math.sqrt(sum);
    }

    public void addPowerPedestal(WorldBlockPos worldBlockPos, int level) {
        this.powerPedestals.put(worldBlockPos, level);
    }

    public void addEfficiencyPedestal(WorldBlockPos worldBlockPos, int level) {
        this.efficiencyPedestals.put(worldBlockPos, level);
    }

    public void addCooldownPedestal(WorldBlockPos worldBlockPos, int level) {
        this.cooldownPedestals.put(worldBlockPos, level);
    }

}
