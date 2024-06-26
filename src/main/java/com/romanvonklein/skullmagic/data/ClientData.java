package com.romanvonklein.skullmagic.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.romanvonklein.skullmagic.networking.ClientPackageSender;
import com.romanvonklein.skullmagic.spells.Spell;
import com.romanvonklein.skullmagic.spells.SpellWithHoldAction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
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

    public ClientData() {
        this.spellnames = new ArrayList<>();
        this.essencePool = new EssencePool();
        this.selectedSpell = "";
        this.spells = new HashMap<>();
    }

    public static ClientData fromNbt(NbtCompound nbt) {
        PlayerData playerData = PlayerData.fromNbt(nbt.getCompound("playerdata"));
        ClientData data = new ClientData(playerData);
        data.spellnames = new ArrayList<String>();
        for (String spellName : playerData.spells.keySet()) {
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
        this.getEssencePool().setEssence(amount, null);
    }

    public void setMaxEssence(int amount) {
        this.getEssencePool().setMaxEssence(amount, null);
    }

    public void setEssenceChargeRate(int amount) {
        this.getEssencePool().setEssenceChargeRate(amount, null);
    }

    public boolean knowsSpell(String spellname) {
        return this.spells.containsKey(spellname) && this.spells.get(spellname) != null;
    }

    public int getCooldownLeftForSpell(String spellname) {
        return this.spells.get(spellname).getCooldownLeft();
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

    public void setSelectedSpellName(String spellName) {
        this.selectedSpell = spellName;
    }

    public void cycleSpell(MinecraftClient clt) {
        if (this.spells.size() > 0) {
            int currentIndex = spellnames.indexOf(this.selectedSpell);
            if (clt.player.isSneaking()) {
                currentIndex--;
            } else {
                currentIndex++;
            }
            currentIndex = adjustForListsize(currentIndex);
            this.selectedSpell = spellnames.get(currentIndex);
        }
    }

    public String getPrevSpellname() {
        String result = null;
        if (!spellnames.isEmpty()) {
            int currentIndex = adjustForListsize(spellnames.indexOf(this.selectedSpell) - 1);
            result = spellnames.get(currentIndex);
        }
        return result;
    }

    public String getNextSpellname() {
        String result = null;
        if (!spellnames.isEmpty()) {
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
        if (this.essencePool.getAltarPos() != null && this.essencePool.getWorldKey() != null) {
            results.add(new WorldBlockPos(this.essencePool.getAltarPos(), this.essencePool.getWorldKey()));
        }
        return results;
    }

    public ArrayList<BlockPos> getConnectedSkullPedestals() {
        ArrayList<BlockPos> results = new ArrayList<>();
        for (BlockPos pos : this.essencePool.getPedestalPositions()) {
            results.add(pos);
        }
        return results;
    }

    public RegistryKey<World> getSkullAltarWorldKey() {
        return this.essencePool.getWorldKey();
    }

    public ArrayList<WorldBlockPos> getActiveSpellShrinesWorldBlockPos() {
        ArrayList<WorldBlockPos> results = new ArrayList<>();
        for (String learnedSpellname : this.spells.keySet()) {
            SpellShrineData spellShrineData = this.spells.get(learnedSpellname).spellShrine;
            if (spellShrineData.shrinePos != null && spellShrineData.worldKey != null) {
                results.add(new WorldBlockPos(spellShrineData.shrinePos, spellShrineData.worldKey));
            }
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
        if (this.essencePool.getAltarPos() == null) {
            return null;
        }
        return new WorldBlockPos(this.essencePool.getAltarPos(), this.essencePool.getWorldKey());
    }

    public boolean isLinkedToAltar() {
        return this.getEssencePool().getAltarPos() != null;
    }

    public SpellData getSelectedSpellData() {
        return this.spells.get(this.selectedSpell);
    }

    public void tick(MinecraftClient client) {
        // tick the active spell if applicable
        if (this.selectedSpell != null && !this.selectedSpell.equals("")) {
            Spell selectedSpell = ServerData.getSpells().get(this.selectedSpell);
            if (selectedSpell instanceof SpellWithHoldAction) {
                ((SpellWithHoldAction) selectedSpell).clientAction.apply(client.player,
                        this.getSpellPower(this.selectedSpell));
            }
        }
    }

    public void checkAutoCasts() {
        for (Map.Entry<String, SpellData> entry : this.spells.entrySet()) {
            if (entry.getValue().autoCast() && !entry.getValue().isOnCooldown()
                    && this.canAfford(entry.getValue().getEssenceCost())) {
                ClientPackageSender.sendCastSpellPackage(entry.getKey());
            }
        }
    }

    public boolean shouldAutocastSpell(String spellname) {
        return this.spells.get(spellname).autoCast();
    }

    public SpellShrineData getSpellShrineAt(WorldBlockPos shrinePos) {
        SpellShrineData result = null;
        for (SpellData spellData : this.spells.values()) {
            if (shrinePos.isEqualTo(spellData.getShrinePos()) && shrinePos.worldKey.equals(spellData.getWorldKey())) {
                result = spellData.spellShrine;
                break;
            }
        }
        return result;

    }
}