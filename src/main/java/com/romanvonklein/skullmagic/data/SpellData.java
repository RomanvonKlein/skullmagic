package com.romanvonklein.skullmagic.data;

import java.util.UUID;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.spells.Spell;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public class SpellData extends PersistentState {
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
        this.cooldownLevel = cooldownLevel2;
        this.baseCooldown = ServerData.getSpells().get(spellname).cooldownTicks;
        this.baseCost = ServerData.getSpells().get(spellname).essenceCost;
    }

    public boolean isDirty() {
        return true;
    }

    static SpellData fromNbt(NbtCompound tag, String spellname) {

        // spellShrine
        SpellShrineData spellShrineData = SpellShrineData.fromNbt(tag.getCompound("spellShrine"));

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

    public void removeSpellShrine(UUID playerToUpdate) {
        this.spellShrine = new SpellShrineData();
        this.powerLevel = 1.0;
        this.efficiencyLevel = 1.0;
        this.cooldownLevel = 1.0;
        SkullMagic.updatePlayer(playerToUpdate);
    }

    public BlockPos getShrinePos() {
        return this.spellShrine.shrinePos;
    }

    public boolean containsPedestal(WorldBlockPos worldBlockPos) {
        boolean result = false;
        outer: if (this.spellShrine.worldKey != null
                && this.spellShrine.worldKey.toString().equals(worldBlockPos.worldKey.toString())) {
            for (BlockPos pos : this.spellShrine.powerPedestals.keySet()) {
                if (worldBlockPos.isEqualTo(pos)) {
                    result = true;
                    break outer;
                }
            }
            for (BlockPos pos : this.spellShrine.efficiencyPedestals.keySet()) {
                if (worldBlockPos.isEqualTo(pos)) {
                    result = true;
                    break outer;
                }
            }
            for (BlockPos pos : this.spellShrine.cooldownPedestals.keySet()) {
                if (worldBlockPos.isEqualTo(pos)) {
                    result = true;
                    break outer;
                }
            }
        }
        return result;
    }

    public void addSpellPedestal(WorldBlockPos worldBlockPos, UUID playerToUpdate, String type, int level) {
        if (type.equals("power")) {
            this.spellShrine.addPowerPedestal(worldBlockPos, level);
            this.powerLevel = this.spellShrine.getPowerLevel();
        } else if (type.equals("efficiency")) {
            this.spellShrine.addEfficiencyPedestal(worldBlockPos, level);
            this.efficiencyLevel = this.spellShrine.getEfficiencyLevel();

        } else if (type.equals("cooldown")) {
            this.spellShrine.addCooldownPedestal(worldBlockPos, level);
            this.cooldownLevel = this.spellShrine.getCooldownLevel();

        } else {
            throw new IllegalArgumentException("Type: '" + type + "' is not a valid spellpedestal type.");
        }
        SkullMagic.updatePlayer(playerToUpdate);
    }

    public boolean tryRemoveSpellPedestal(WorldBlockPos worldBlockPos, UUID playerToUpdate) {
        return this.tryRemoveSpellPowerPedestal(worldBlockPos, playerToUpdate)
                || this.tryRemoveSpellCooldownPedestal(worldBlockPos, playerToUpdate)
                || this.tryRemoveSpellEfficiencyPedestal(worldBlockPos, playerToUpdate);
    }

    public boolean tryRemoveSpellPowerPedestal(WorldBlockPos worldBlockPos, UUID playerToUpdate) {
        boolean result = false;
        BlockPos posToRemove = null;
        if (this.spellShrine.worldKey != null
                && this.spellShrine.worldKey.toString().equals(worldBlockPos.worldKey.toString())) {
            for (BlockPos pos : this.spellShrine.powerPedestals.keySet()) {
                if (worldBlockPos.isEqualTo(pos)) {
                    posToRemove = pos;
                    result = true;
                    break;
                }
            }
        }
        if (result) {
            this.spellShrine.powerPedestals.remove(posToRemove);

            SkullMagic.updatePlayer(playerToUpdate);
        }
        return result;
    }

    public boolean tryRemoveSpellEfficiencyPedestal(WorldBlockPos worldBlockPos, UUID playerToUpdate) {
        boolean result = false;
        BlockPos posToRemove = null;
        if (this.spellShrine.worldKey != null
                && this.spellShrine.worldKey.toString().equals(worldBlockPos.worldKey.toString())) {
            for (BlockPos pos : this.spellShrine.efficiencyPedestals.keySet()) {
                if (worldBlockPos.isEqualTo(pos)) {
                    posToRemove = pos;
                    result = true;
                    break;
                }
            }
        }
        if (result) {
            this.spellShrine.efficiencyPedestals.remove(posToRemove);

            SkullMagic.updatePlayer(playerToUpdate);
        }
        return result;
    }

    public boolean tryRemoveSpellCooldownPedestal(WorldBlockPos worldBlockPos, UUID playerToUpdate) {
        boolean result = false;
        BlockPos posToRemove = null;
        if (this.spellShrine.worldKey != null
                && this.spellShrine.worldKey.toString().equals(worldBlockPos.worldKey.toString())) {
            for (BlockPos pos : this.spellShrine.cooldownPedestals.keySet()) {
                if (worldBlockPos.isEqualTo(pos)) {
                    posToRemove = pos;
                    result = true;
                    break;
                }
            }
        }
        if (result) {
            this.spellShrine.cooldownPedestals.remove(posToRemove);

            SkullMagic.updatePlayer(playerToUpdate);
        }
        return result;
    }

    public int getCooldownLeft() {
        return this.cooldownLeft;
    }

    public void tick(UUID playerToUpdate) {
        if (this.cooldownLeft > 0) {
            this.cooldownLeft--;
            SkullMagic.updatePlayer(playerToUpdate);
        }
    }
}
