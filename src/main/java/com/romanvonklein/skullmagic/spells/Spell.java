package com.romanvonklein.skullmagic.spells;

import org.apache.commons.lang3.function.TriFunction;

import com.romanvonklein.skullmagic.persistantState.EssencePool;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class Spell {

    public int essenceCost;
    // TODO: implement cooldown
    public int cooldownTicks;

    public TriFunction<ServerPlayerEntity, World, EssencePool, Boolean> action;

    public Spell(int essenceCost, int cooldownTicks,
            TriFunction<ServerPlayerEntity, World, EssencePool, Boolean> action) {
        this.essenceCost = essenceCost;
        this.cooldownTicks = cooldownTicks;
        this.action = action;
    }

}
