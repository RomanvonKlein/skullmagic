package com.romanvonklein.skullmagic.spells;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class SpellWithHoldAction extends Spell {

    protected SpellWithHoldAction(int essenceCost, int cooldownTicks, int learnLevelCost, boolean isTargeted) {
        super(essenceCost, cooldownTicks, learnLevelCost, isTargeted);
    }

    public abstract boolean serverAction(ServerPlayerEntity player, double powerLevel);

    public abstract boolean clientAction(ClientPlayerEntity player, double powerLevel);

}
