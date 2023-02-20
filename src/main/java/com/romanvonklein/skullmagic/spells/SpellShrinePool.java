package com.romanvonklein.skullmagic.spells;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.networking.ServerPackageSender;
import com.romanvonklein.skullmagic.util.Parsing;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public class SpellShrinePool extends PersistentState {

    BlockPos position;
    public UUID linkedPlayerID;
    HashMap<BlockPos, Integer> linkedPowerPedestals = new HashMap<>();
    HashMap<BlockPos, Integer> linkedEfficiencyPedestals = new HashMap<>();
    HashMap<BlockPos, Integer> linkedCooldownPedestals = new HashMap<>();
    private String spellName = "";
    int range = 5;
    public int strength = 0;

    public SpellShrinePool(UUID playerID, BlockPos pos, String spellname, int range) {
        this.linkedPlayerID = playerID;
        this.position = pos;
        this.spellName = spellname;
        this.range = range;
    }

    public SpellShrinePool() {
    }

    public SpellShrinePool(BlockPos pos) {
        this.position = pos;
    }

    public NbtCompound writeNbt(NbtCompound tag) {
        if (this.linkedPlayerID != null) {
            tag.putUuid("linkedPlayerID", this.linkedPlayerID);
        }
        tag.putString("spellname", this.spellName);
        tag.putInt("strength", this.strength);
        tag.putInt("range", this.range);
        NbtCompound altarPosition = new NbtCompound();
        altarPosition.putInt("x", this.position.getX());
        altarPosition.putInt("y", this.position.getY());
        altarPosition.putInt("z", this.position.getZ());
        tag.put("altarPosition", altarPosition);

        NbtCompound linkedPowerPedestalsNBT = new NbtCompound();
        for (Entry<BlockPos, Integer> entry : this.linkedPowerPedestals.entrySet()) {
            linkedPowerPedestalsNBT.putInt(entry.getKey().toShortString(), entry.getValue());
        }
        tag.put("linkedPowerPedestals", linkedPowerPedestalsNBT);

        NbtCompound linkedEfficiencyPedestalsNBT = new NbtCompound();
        for (Entry<BlockPos, Integer> entry : this.linkedEfficiencyPedestals.entrySet()) {
            linkedEfficiencyPedestalsNBT.putInt(entry.getKey().toShortString(), entry.getValue());
        }
        tag.put("linkedEfficiencyPedestals", linkedEfficiencyPedestalsNBT);

        NbtCompound linkedCooldownPedestalsNBT = new NbtCompound();
        for (Entry<BlockPos, Integer> entry : this.linkedCooldownPedestals.entrySet()) {
            linkedCooldownPedestalsNBT.putInt(entry.getKey().toShortString(), entry.getValue());
        }
        tag.put("linkedCooldownPedestals", linkedCooldownPedestalsNBT);
        return tag;
    }

    public static SpellShrinePool fromNbt(NbtCompound tag) {
        SpellShrinePool pool = new SpellShrinePool();
        if (tag.contains("linkedPlayerID")) {
            pool.linkedPlayerID = tag.getUuid("linkedPlayerID");
        }
        pool.strength = tag.getInt("strength");
        pool.spellName = tag.getString("spellname");
        pool.range = tag.getInt("range");
        NbtCompound altarPosition = tag.getCompound("altarPosition");
        int x = altarPosition.getInt("x");
        int y = altarPosition.getInt("y");
        int z = altarPosition.getInt("z");
        pool.position = new BlockPos(x, y, z);

        NbtCompound powerPedestalList = tag.getCompound("linkedPowerPedestals");
        powerPedestalList.getKeys().forEach((shortString) -> {
            pool.linkedPowerPedestals.put(Parsing.shortStringToBlockPos(shortString),
                    powerPedestalList.getInt(shortString));
        });

        NbtCompound efficiencyPedestalList = tag.getCompound("linkedEfficiencyPedestals");
        efficiencyPedestalList.getKeys().forEach((shortString) -> {
            pool.linkedEfficiencyPedestals.put(Parsing.shortStringToBlockPos(shortString),
                    efficiencyPedestalList.getInt(shortString));
        });

        NbtCompound cooldownPedestalList = tag.getCompound("linkedCooldownPedestals");
        cooldownPedestalList.getKeys().forEach((shortString) -> {
            pool.linkedCooldownPedestals.put(Parsing.shortStringToBlockPos(shortString),
                    cooldownPedestalList.getInt(shortString));
        });

        return pool;
    }

    public void removePedestal(ServerWorld world, BlockPos pos, String type) {
        boolean changed = true;
        if (type.equals("power")) {
            int lostStrength = this.linkedPowerPedestals.get(pos);
            this.removePower(lostStrength);
            this.linkedPowerPedestals.remove(pos);
        } else if (type.equals("efficiency")) {
            int lostStrength = this.linkedEfficiencyPedestals.get(pos);
            this.removeEfficiency(lostStrength);
            this.linkedEfficiencyPedestals.remove(pos);
        } else if (type.equals("cooldown")) {
            int lostStrength = this.linkedCooldownPedestals.get(pos);
            this.removeCooldown(lostStrength);
            this.linkedCooldownPedestals.remove(pos);
        } else {
            changed = false;
            SkullMagic.LOGGER.warn("Unknown spell pedestal type: '" + type + "'");
        }

        if (changed) {
            ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(this.linkedPlayerID);
            if (player != null) {
                ServerPackageSender.sendUpdateLinksPackage(player);
            }
        }

    }

    public boolean setSpellName(String spellname) {
        boolean result = false;
        if (this.spellName.equals("")) {
            this.spellName = spellname;
            result = true;
        }
        return result;
    }

    public String getSpellName() {
        return this.spellName;
    }

    // public JsonObject toJsonElement() {
    // JsonObject elem = new JsonObject();
    public void addPowerPedestal(ServerWorld world, BlockPos candidatePos, int strength) {
        this.linkedPowerPedestals.put(candidatePos, strength);
        addPower(strength);
        ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(this.linkedPlayerID);
        if (player != null) {
            ServerPackageSender.sendUpdateLinksPackage(player);
        }
    }

    public void addEfficiencyPedestal(ServerWorld world, BlockPos candidatePos, int strength) {
        this.linkedEfficiencyPedestals.put(candidatePos, strength);
        addEfficiency(strength);
        ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(this.linkedPlayerID);
        if (player != null) {
            ServerPackageSender.sendUpdateLinksPackage(player);
        }
    }

    public void addCooldownPedestal(ServerWorld world, BlockPos candidatePos, int strength) {
        this.linkedCooldownPedestals.put(candidatePos, strength);
        addCooldown(strength);
        ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(this.linkedPlayerID);
        if (player != null) {
            ServerPackageSender.sendUpdateLinksPackage(player);
        }
    }

    private void addPower(int strengthWon) {
        this.strength += strengthWon;
        if (this.linkedPlayerID != null) {
            PlayerSpellData data = SkullMagic.spellManager.getSpellData(this.linkedPlayerID, this.spellName);
            if (data != null) {
                data.setPowerLevel(1
                        + Math.floor(Math.sqrt(this.strength)));
            }
        }
    }

    private void addEfficiency(int strengthWon) {
        this.strength += strengthWon;
        if (this.linkedPlayerID != null) {
            PlayerSpellData data = SkullMagic.spellManager.getSpellData(this.linkedPlayerID, this.spellName);
            if (data != null) {
                data.setEfficiencyLevel(1
                        + Math.floor(Math.sqrt(this.strength)));
            }
        }

    }

    private void addCooldown(int strengthWon) {
        this.strength += strengthWon;
        if (this.linkedPlayerID != null) {
            PlayerSpellData data = SkullMagic.spellManager.getSpellData(this.linkedPlayerID, this.spellName);
            if (data != null) {
                data.setCooldownLevel(1
                        + Math.floor(Math.sqrt(this.strength)));
            }
        }

    }

    private void removePower(int strengthLost) {
        this.strength -= strengthLost;
        if (this.linkedPlayerID != null) {
            PlayerSpellData data = SkullMagic.spellManager.getSpellData(this.linkedPlayerID, this.spellName);
            if (data != null) {
                data.setPowerLevel(1
                        + Math.floor(Math.sqrt(this.strength)));
            }
        }
    }

    private void removeEfficiency(int strengthLost) {
        this.strength -= strengthLost;
        if (this.linkedPlayerID != null) {
            PlayerSpellData data = SkullMagic.spellManager.getSpellData(this.linkedPlayerID, this.spellName);
            if (data != null) {
                data.setEfficiencyLevel(1
                        + Math.floor(Math.sqrt(this.strength)));
            }
        }
    }

    private void removeCooldown(int strengthLost) {
        this.strength -= strengthLost;
        if (this.linkedPlayerID != null) {
            PlayerSpellData data = SkullMagic.spellManager.getSpellData(this.linkedPlayerID, this.spellName);
            if (data != null) {
                data.setCooldownLevel(1
                        + Math.floor(Math.sqrt(this.strength)));
            }
        }
    }
}
