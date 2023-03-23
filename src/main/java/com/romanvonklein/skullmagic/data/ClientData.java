package com.romanvonklein.skullmagic.data;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class ClientData extends PlayerData {

    private ArrayList<String> spellnames;

    public ClientData(HashMap<String, SpellData> spells, EssencePool essencePool, String selectedSpell,
            ArrayList<String> spellnames) {
        super(spells, essencePool, selectedSpell);
        this.spellnames = spellnames;
    }

    public ClientData(PlayerData data, ArrayList<String> spellnames) {
        super(data.spells, data.essencePool, data.selectedSpell);
        this.spellnames = spellnames;
    }

    private ClientData(PlayerData data) {
        super(data.spells, data.essencePool, data.selectedSpell);
    }

    public static ClientData fromNbt(NbtCompound nbt) {
        PlayerData playerData = PlayerData.fromNbt(nbt.getCompound("playerdata"));
        ClientData data = new ClientData(playerData);
        data.spellnames = new ArrayList<String>();
        for (String spellName : nbt.getString("spellnames").split(";")) {
            data.spellnames.add(spellName);
        }
        return data;
    }

    public int getCurrentEssence() {
        return this.getEssencePool().getEssence();
    }

    public int getMaxEssence() {
        return this.getEssencePool().getMaxEssence();
    }

    public void setEssence(int amount) {
        this.getEssencePool().setEssence(amount);
    }

    public void setMaxEssence(int amount) {
        this.getEssencePool().setMaxEssence(amount);
    }

    public void setEssenceChargeRate(int amount) {
        this.getEssencePool().setEssenceChargeRate(amount);
    }

    public boolean hasSpell(String spellname) {
        return this.spells.containsKey(spellname) && this.spells.get(spellname) != null;
    }

    public int getCooldownLeftForSpell(String spellname) {
        return 0;
    }

    public int getMaxCooldownForSpell(String spellname) {
        return this.spells.get(spellname).getMaxCoolDown();
    }

    public double getPowerLevel(String spellName) {
        return this.spells.get(spellName).getPowerLevel();
    }

    public double getEfficiencyLevel(String spellName) {
        return this.spells.get(spellName).getEfficiencyLevel();
    }

    public double getCooldownLevel(String spellName) {
        return this.spells.get(spellName).getCooldownLevel();
    }

    public void cycleSpell() {
        if (this.spells.size() > 0) {

            int currentIndex = spellnames.indexOf(this.selectedSpell);
            currentIndex++;
            currentIndex = adjustForListsize(currentIndex);
            this.selectedSpell = spellnames.get(currentIndex);
        }
    }

    public String getPrevSpellname() {
        String result = null;
        if (spellnames.size() > 0) {
            int currentIndex = adjustForListsize(spellnames.indexOf(this.selectedSpell) - 1);
            result = spellnames.get(currentIndex);
        }
        return result;
    }

    public String getNextSpellname() {
        String result = null;
        if (spellnames.size() > 0) {
            int currentIndex = adjustForListsize(spellnames.indexOf(this.selectedSpell) + 1);
            result = spellnames.get(currentIndex);
        }
        return result;
    }

    private int adjustForListsize(int index) {
        return index >= spellnames.size() ? 0 : index < 0 ? spellnames.size() - 1 : index;
    }

    public String getSelectedSpellName() {
        return this.selectedSpell;
    }

    public ArrayList<WorldBlockPos> getActiveAltarsWorldBlockPos() {
        ArrayList<WorldBlockPos> results = new ArrayList<>();
        if (this.essencePool.altarPos != null && this.essencePool.worldKey != null) {
            results.add(new WorldBlockPos(this.essencePool.altarPos, this.essencePool.worldKey));
        }
        return results;
    }

    public ArrayList<BlockPos> getConnectedSkullPedestals() {
        ArrayList<BlockPos> results = new ArrayList<>();
        for (BlockPos pos : this.essencePool.pedestals.keySet()) {
            results.add(pos);
        }
        return results;
    }

    public RegistryKey<World> getSkullAltarWorldKey() {
        return this.essencePool.worldKey;
    }

    public ArrayList<WorldBlockPos> getActiveSpellShrinesWorldBlockPos() {
        ArrayList<WorldBlockPos> results = new ArrayList<>();
        for (String learnedSpellname : this.spells.keySet()) {
            SpellShrineData spellShrineData = this.spells.get(learnedSpellname).spellShrine;
            results.add(new WorldBlockPos(spellShrineData.shrinePos, spellShrineData.worldKey));
        }
        return results;
    }

    public ArrayList<BlockPos> getSpellPedestalsForSpellAltar(WorldBlockPos shrinePos) {
        ArrayList<BlockPos> results = new ArrayList<>();
        for (String learnedSpellname : this.spells.keySet()) {
            SpellShrineData spellShrineData = this.spells.get(learnedSpellname).spellShrine;
            // get all the different pedestals
            for (BlockPos pedestalPos : spellShrineData.cooldownPedestals.keySet()) {
                results.add(pedestalPos);
            }
            for (BlockPos pedestalPos : spellShrineData.efficiencyPedestals.keySet()) {
                results.add(pedestalPos);
            }
            for (BlockPos pedestalPos : spellShrineData.powerPedestals.keySet()) {
                results.add(pedestalPos);
            }
        }
        return results;
    }

    public WorldBlockPos getActiveAltarWorldBlockPos() {
        if(this.essencePool.altarPos==null){
            return null;
        }
        return new WorldBlockPos(this.essencePool.altarPos, this.essencePool.worldKey);
    }
}