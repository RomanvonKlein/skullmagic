package com.romanvonklein.skullmagic.data;

import java.util.UUID;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.spells.Spell;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

class SpellData extends PersistentState {
    SpellShrineData spellShrine;
    private int cooldownLeft;
    private int baseCooldown;
    private double powerLevel;
    private int baseCost;

    public SpellData(Spell spell) {
        this.spellShrine = new SpellShrineData();
        this.cooldownLeft = 0;
        this.baseCooldown = spell.cooldownTicks;
        this.powerLevel = 1.0;
        this.efficiencyLevel = 1.0;
        this.cooldownLevel = 1.0;
        this.baseCost = spell.essenceCost;
    }

    public void setPowerLevel(double powerLevel, UUID playerToUpdate) {
        this.powerLevel = powerLevel;
        SkullMagic.updatePlayer(playerToUpdate);
    }

    private double efficiencyLevel;

    public void setEfficiencyLevel(double efficiencyLevel, UUID playerToUpdate) {
        this.efficiencyLevel = efficiencyLevel;
        SkullMagic.updatePlayer(playerToUpdate);
    }

    private double cooldownLevel;

    public void setCooldownLevel(double cooldownLevel, UUID playerToUpdate) {
        this.cooldownLevel = cooldownLevel;
        SkullMagic.updatePlayer(playerToUpdate);
    }

    SpellData(String spellname, SpellShrineData spellShrine, int cooldownLeft, double powerLevel2,
            double efficiencyLevel2, double cooldownLevel2) {
        this.spellShrine = spellShrine;
        this.cooldownLeft = cooldownLeft;
        this.powerLevel = powerLevel2;
        this.efficiencyLevel = efficiencyLevel2;
        this.cooldownLeft = cooldownLeft;
        this.baseCooldown = ServerData.getSpells().get(spellname).cooldownTicks;
        this.baseCost = ServerData.getSpells().get(spellname).essenceCost;
    }

    public boolean isDirty() {
        return true;
    }

    static SpellData fromNbt(NbtCompound tag, String spellname) {

        // spellShrine
        SpellShrineData spellShrineData = SpellShrineData.fromNbt(tag);

        // cooldownleft
        int cooldownLeft = tag.getInt("cooldownLeft");

        // get upgrades from spell shrines
        double powerLevel = spellShrineData.getPowerLevel();
        double efficiencyLevel = spellShrineData.getEfficiencyLevel();
        double cooldownLevel = spellShrineData.getCooldownLevel();

        return new SpellData(spellname, spellShrineData, cooldownLeft, powerLevel, efficiencyLevel, cooldownLevel);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        // spellShrine
        NbtCompound spellShrineNbt = new NbtCompound();
        spellShrine.writeNbt(spellShrineNbt);
        tag.put("spellShrine", spellShrineNbt);

        // cooldownLeft
        tag.putInt("cooldownLeft", this.cooldownLeft);

        return tag;
    }

    public double getPowerLevel() {
        return this.powerLevel;
    }

    public double getEfficiencyLevel() {
        return this.efficiencyLevel;
    }

    public double getCooldownLevel() {
        return this.cooldownLevel;
    }

    public boolean isOnCooldown() {
        return this.cooldownLeft > 0;
    }

    public int getEssenceCost() {
        return (int) Math
                .round(Double.valueOf(baseCost) - Double.valueOf(baseCost) * (Math.log(this.efficiencyLevel) / 4.0));
    }

    public int getMaxCoolDown() {
        return (int) Math.round(
                Double.valueOf(baseCooldown) - Double.valueOf(baseCooldown) * (Math.log(this.cooldownLevel) / 4.0));
    }

    public void setOnCooldown(UUID playerToUpdate) {
        this.cooldownLeft = this.getMaxCoolDown();
        SkullMagic.updatePlayer(playerToUpdate);
    }

}
