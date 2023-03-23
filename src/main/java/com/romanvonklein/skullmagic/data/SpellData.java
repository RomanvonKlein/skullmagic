package com.romanvonklein.skullmagic.data;

import com.romanvonklein.skullmagic.spells.Spell;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

class SpellData extends PersistentState {
     SpellShrineData spellShrine;
    private int cooldownLeft;
    private int maxCoolDown;
    private double powerLevel;

    public SpellData(Spell spell) {
        this.spellShrine = new SpellShrineData();
        cooldownLeft = 0;
        maxCoolDown = spell.cooldownTicks;
        powerLevel = 1.0;
    }

    public void setPowerLevel(double powerLevel) {
        this.powerLevel = powerLevel;
    }

    private double efficiencyLevel;

    public void setEfficiencyLevel(double efficiencyLevel) {
        this.efficiencyLevel = efficiencyLevel;
    }

    private double cooldownLevel;

    public void setCooldownLevel(double cooldownLevel) {
        this.cooldownLevel = cooldownLevel;
    }

    SpellData(SpellShrineData spellShrine, int cooldownLeft) {
        this.spellShrine = spellShrine;
        this.cooldownLeft = cooldownLeft;
    }

    public boolean isDirty() {
        return true;
    }

    static SpellData fromNbt(NbtCompound tag) {
        // spellShrine
        SpellShrineData spellShrineData = SpellShrineData.fromNbt(tag);

        // cooldownleft
        int cooldownLeft = tag.getInt("cooldownLeft");
        return new SpellData(spellShrineData, cooldownLeft);
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

    public int getMaxCoolDown() {
        return this.maxCoolDown;
    }

    public void setMaxCoolDown(int amount) {
        this.maxCoolDown = amount;
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

}
