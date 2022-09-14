package com.romanvonklein.skullmagic.spells;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.util.Parsing;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public class SpellShrinePool extends PersistentState {

    BlockPos position;
    public UUID linkedPlayerID;
    HashMap<BlockPos, Integer> linkedPedestals = new HashMap<>();
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
        NbtCompound linkedPedestalsNBT = new NbtCompound();
        for (Entry<BlockPos, Integer> entry : this.linkedPedestals.entrySet()) {
            linkedPedestalsNBT.putInt(entry.getKey().toShortString(), entry.getValue());
        }
        tag.put("linkedPedestals", linkedPedestalsNBT);
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
        NbtCompound pedestalList = tag.getCompound("linkedPedestals");
        pedestalList.getKeys().forEach((shortString) -> {
            pool.linkedPedestals.put(Parsing.shortStringToBlockPos(shortString), pedestalList.getInt(shortString));
        });
        return pool;
    }

    public void removePedestal(BlockPos pos) {
        int lostStrength = this.linkedPedestals.get(pos);
        this.removeStrength(lostStrength);
        this.linkedPedestals.remove(pos);
    }

    public void linkPedestal(BlockPos pedestalPos, int strength) {
        this.linkedPedestals.put(pedestalPos, strength);
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

    public void addPedestal(BlockPos candidatePos, int strength) {
        this.linkedPedestals.put(candidatePos, strength);
        addStrength(strength);
    }

    private void addStrength(int strengthWon) {
        this.strength += strengthWon;
        if (this.linkedPlayerID != null) {
            PlayerSpellData data = SkullMagic.spellManager.getSpellData(this.linkedPlayerID, this.spellName);
            if (data != null) {
                data.setPowerLevel(1
                        + Math.floor(Math.sqrt(this.strength)));
            }
        }

    }

    private void removeStrength(int strengthLost) {
        this.strength -= strengthLost;
        if (this.linkedPlayerID != null) {
            PlayerSpellData data = SkullMagic.spellManager.getSpellData(this.linkedPlayerID, this.spellName);
            if (data != null) {
                data.setPowerLevel(1
                        + Math.floor(Math.sqrt(this.strength)));
            }
        }
    }

    // elem.addProperty("position", this.position.toShortString());
    // JsonArray consumerList = new JsonArray();
    // for (BlockPos pos : this.Consumers) {
    // consumerList.add(pos.toShortString());
    // }
    // elem.add("consumers", consumerList);
    // return elem;
    // }
}
