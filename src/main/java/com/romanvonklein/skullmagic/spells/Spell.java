package com.romanvonklein.skullmagic.spells;

import java.util.function.BiFunction;

import net.minecraft.server.network.ServerPlayerEntity;

public class Spell {

    public int essenceCost;
    public int cooldownTicks;
    public int learnLevelCost;
    public BiFunction<ServerPlayerEntity, Double, Boolean> action;
    public boolean isTargeted;

    public Spell(int essenceCost, int cooldownTicks, int learnLevelCost, boolean isTargeted,
            BiFunction<ServerPlayerEntity, Double, Boolean> action) {
        this.essenceCost = essenceCost;
        this.cooldownTicks = cooldownTicks;
        this.learnLevelCost = learnLevelCost;
        this.action = action;
        this.isTargeted = isTargeted;
    }

}
