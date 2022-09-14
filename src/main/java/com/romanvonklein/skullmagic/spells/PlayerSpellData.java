package com.romanvonklein.skullmagic.spells;

import com.romanvonklein.skullmagic.SkullMagic;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

public class PlayerSpellData extends PersistentState {
    public int cooldownLeft;
    public double efficiencyLevel;
    public double powerLevel;
    public double cooldownReductionLevel;

    public PlayerSpellData(int cooldownLeft, double efficiencyLevel, double powerLevel,
            double cooldownReductionLevel) {
        this.cooldownLeft = cooldownLeft;
        this.efficiencyLevel = efficiencyLevel;
        this.powerLevel = powerLevel;
        this.cooldownReductionLevel = cooldownReductionLevel;
    }

    public PlayerSpellData() {
        this.cooldownLeft = 0;
        this.efficiencyLevel = 0.0;
        this.powerLevel = 0.0;
        this.cooldownReductionLevel = 0.0;

    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("cooldown_remaining", this.cooldownLeft);
        nbt.putDouble("power_level", this.powerLevel);
        nbt.putDouble("efficiency_level", this.efficiencyLevel);
        nbt.putDouble("cooldownreduction_level", this.cooldownReductionLevel);
        return nbt;
    }

    public static PlayerSpellData fromNbt(NbtCompound tag) {
        PlayerSpellData result;
        if (tag.contains("cooldown_remaining", NbtType.INT) && tag.contains("efficiency_level", NbtType.DOUBLE)
                && tag.contains("power_level", NbtType.DOUBLE)
                && tag.contains("cooldownreduction_level", NbtType.DOUBLE)) {

            result = new PlayerSpellData(tag.getInt("cooldown_remaining"),
                    tag.getDouble("efficiency_level"), tag.getDouble("power_level"),
                    tag.getDouble("cooldownreduction_level"));
        } else {
            result = new PlayerSpellData(0, 1.0, 1.0, 1.0);
        }
        return result;
    }

    public int getMaxCooldown(int defaultCooldownTicks) {
        return (int) Math.round(defaultCooldownTicks
                * (1 - Math.log(1 + (this.cooldownReductionLevel - 1) * 0.5)));
    }
}
