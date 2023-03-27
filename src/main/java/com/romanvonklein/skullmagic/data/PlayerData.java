package com.romanvonklein.skullmagic.data;

import java.util.HashMap;
import java.util.UUID;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

class PlayerData extends PersistentState {
    protected HashMap<String, SpellData> spells;
    protected EssencePool essencePool;
    protected String selectedSpell;

    public PlayerData() {
        this.spells = new HashMap<>();
        this.essencePool = new EssencePool();
        this.selectedSpell = "";
    }

    PlayerData(HashMap<String, SpellData> spells, EssencePool essencePool, String selectedSpell) {
        this.spells = spells;
        this.essencePool = essencePool;
        this.selectedSpell = selectedSpell;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        // spells
        NbtCompound spellsCompound = new NbtCompound();
        for (String spellName : spells.keySet()) {
            NbtCompound spellDataCompound = new NbtCompound();
            spells.get(spellName).writeNbt(spellDataCompound);
            spellsCompound.put(spellName, spellDataCompound);
        }
        tag.put("spells", spellsCompound);

        // essencePool
        NbtCompound essencePoolCompound = new NbtCompound();
        tag.put("essencePool", essencePool.writeNbt(essencePoolCompound));

        // selectedSpell
        tag.putString("selectedSpell", selectedSpell);

        return tag;
    }

    public static PlayerData fromNbt(NbtCompound tag) {
        // spells
        HashMap<String, SpellData> spells = new HashMap<>();
        for (String spellname : tag.getCompound("spells").getKeys()) {
            spells.put(spellname, SpellData.fromNbt(tag.getCompound("spells").getCompound(spellname), spellname));
        }

        // essencePool
        EssencePool essencePool = EssencePool.fromNbt(tag.getCompound("essencePool"));

        // selectedSpell
        String selectedSpell = tag.getString("selectedSpell");
        return new PlayerData(spells, essencePool, selectedSpell);
    }

    EssencePool getEssencePool() {
        return this.essencePool;
    }

    public boolean knowsSpell(String spellname) {
        return this.spells.containsKey(spellname);
    }

    public boolean isSpellOffCoolown(String spellname) {
        return knowsSpell(spellname) && !this.spells.get(spellname).isOnCooldown();
    }

    public void learnSpell(String spellname, SpellData spellData, UUID playerToUpdate) {
        this.spells.put(spellname, spellData);
        SkullMagic.updatePlayer(playerToUpdate);
    }

    public int getEssenceCostForSpell(String spellname) {
        return this.spells.get(spellname).getEssenceCost();
    }

    public boolean canAfford(int essenceCost) {
        return this.getEssencePool().getEssence() >= essenceCost;
    }

    public double getSpellPower(String spellname) {
        return this.spells.get(spellname).getPowerLevel();
    }

    public void setSpellOnCooldown(String spellname, UUID playerToUpdate) {
        this.spells.get(spellname).setOnCooldown(playerToUpdate);
    }

    public void setEssencePool(EssencePool essencePool2, UUID uuid) {
        this.essencePool = essencePool2;
        SkullMagic.updatePlayer(uuid);
    }

    public BlockPos getAltarPos() {
        return this.getEssencePool().getAltarPos();
    }

    public boolean hasCapacityCrystal(RegistryKey<World> registryKey, BlockPos pos) {
        return this.getEssencePool().getWorldKey().toString().equals(registryKey.toString())
                && this.getEssencePool().containsCapacityCrystal(pos);
    }

    public void removeCapacityCrystal(BlockPos pos, UUID playerID) {
        this.getEssencePool().removeCapacityCrystal(pos, playerID);
    }

}
