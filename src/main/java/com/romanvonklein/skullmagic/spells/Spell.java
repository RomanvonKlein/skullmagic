package com.romanvonklein.skullmagic.spells;

import org.apache.commons.lang3.function.TriFunction;

import com.romanvonklein.skullmagic.essence.EssencePool;

import net.minecraft.server.network.ServerPlayerEntity;

public class Spell {

    public int essenceCost;
    public int cooldownTicks;
    public int learnLevelCost;
    public TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean> action;

    public Spell(int essenceCost, int cooldownTicks, int learnLevelCost,
            TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean> action) {
        this.essenceCost = essenceCost;
        this.cooldownTicks = cooldownTicks;
        this.learnLevelCost = learnLevelCost;
        this.action = action;
    }

}
