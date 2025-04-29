package com.romanvonklein.skullmagic.spells;

import net.minecraft.server.network.ServerPlayerEntity;

public abstract class Spell {

    public int essenceCost;
    public int cooldownTicks;
    public int learnLevelCost;
    public boolean isTargeted;

    protected Spell(int essenceCost, int cooldownTicks, int learnLevelCost, boolean isTargeted) {
        this.essenceCost = essenceCost;
        this.cooldownTicks = cooldownTicks;
        this.learnLevelCost = learnLevelCost;
        this.isTargeted = isTargeted;
    }

    public abstract boolean cast(ServerPlayerEntity player, Double powerLevel);

}
