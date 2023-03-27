package com.romanvonklein.skullmagic.data;

import java.util.HashMap;
import java.util.UUID;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.util.Util;

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
        return this.getEssencePool().getWorldKey() != null
                && this.getEssencePool().getWorldKey().toString().equals(registryKey.toString())
                && this.getEssencePool().containsCapacityCrystal(pos);
    }

    public void removeCapacityCrystal(BlockPos pos, UUID playerID) {
        this.getEssencePool().removeCapacityCrystal(pos, playerID);
    }

    public boolean tryAddConsumer(RegistryKey<World> registryKey, BlockPos pos, UUID playerID) {
        boolean result = false;
        if (this.essencePool.getWorldKey().toString().equals(registryKey.toString())
                && Util.inRange(pos, this.getAltarPos(), Config.getConfig().scanWidth, Config.getConfig().scanHeight)) {
            this.essencePool.addConsumer(pos, playerID);
            result = true;
        }
        return result;
    }

    public boolean isSameAltarPos(WorldBlockPos worldBlockPos) {
        return this.getAltarPos() != null && worldBlockPos.isEqualTo(this.getAltarPos());
    }

    public void removeSpellShrine(BlockPos pos, UUID playerid) {
        for (String spellname : this.spells.keySet()) {
            BlockPos candidatePos = this.spells.get(spellname).getShrinePos();
            if (candidatePos != null && pos.getX() == candidatePos.getX() && pos.getY() == candidatePos.getY()
                    && pos.getZ() == candidatePos.getZ()) {
                this.spells.remove(spellname);
                SkullMagic.updatePlayer(playerid);
                break;
            }
        }
    }

    public boolean hasConsumerAtPos(WorldBlockPos worldPos) {
        return this.getEssencePool().hasConumerAtPos(worldPos);
    }

    public void addSpellShrine(String spellname, RegistryKey<World> registryKey, BlockPos pos,
            HashMap<BlockPos, Integer> powerPedestals,
            HashMap<BlockPos, Integer> efficiencyPedestals, HashMap<BlockPos, Integer> cooldownPedestals,
            UUID playerToUpdate) {
        this.spells.get(spellname).spellShrine = new SpellShrineData(registryKey, pos, powerPedestals,
                efficiencyPedestals, cooldownPedestals);
        SkullMagic.updatePlayer(playerToUpdate);
    }

    public boolean hasSpellPedestal(WorldBlockPos worldBlockPos) {
        boolean result = false;

        for (SpellData data : this.spells.values()) {
            if (data.containsPedestal(worldBlockPos)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean tryAddSpellPedestal(WorldBlockPos worldBlockPos, String spellname, String shrineType,
            int shrineLevel,
            UUID playerToUpdate) {
        boolean result = false;
        SpellData data = this.spells.get(spellname);
        if (data != null) {

            if (Util.inRange(worldBlockPos, data.getShrinePos(), Config.getConfig().shrineRangePerLevel * 1,
                    Config.getConfig().shrineRangePerLevel * shrineLevel)) {// TODO: implement shrine levels here!
                data.addSpellPedestal(worldBlockPos, playerToUpdate, shrineType, shrineLevel);
                result = true;
            }
        }
        return result;
    }

}
