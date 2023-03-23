package com.romanvonklein.skullmagic.data;

import java.util.HashMap;

import com.romanvonklein.skullmagic.util.Parsing;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
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
        tag.putIntArray("shrinePos", new int[] { shrinePos.getX(), shrinePos.getY(), shrinePos.getZ() });

        // worldkey
        tag.putString("worldKey", worldKey.getValue().toString());

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
        int[] shrineCoords = tag.getIntArray("shrinePos");
        BlockPos shrinePos = new BlockPos(shrineCoords[0], shrineCoords[0], shrineCoords[0]);

        // worldkey
        String worldKeyString = tag.getString("worldKey");
        RegistryKey<World> worldKey = RegistryKey.of(net.minecraft.util.registry.Registry.WORLD_KEY,
                Identifier.tryParse(worldKeyString));

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

}
