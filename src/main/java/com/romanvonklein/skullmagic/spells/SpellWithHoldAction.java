package com.romanvonklein.skullmagic.spells;

import java.util.function.BiFunction;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class SpellWithHoldAction extends Spell {
    public BiFunction<ServerPlayerEntity, Double, Boolean> serverAction;
    public BiFunction<ClientPlayerEntity, Double, Boolean> clientAction;

    public SpellWithHoldAction(int essenceCost, int cooldownTicks, int learnLevelCost,
            BiFunction<ServerPlayerEntity, Double, Boolean> action,
            BiFunction<ServerPlayerEntity, Double, Boolean> serverAction,
            BiFunction<ClientPlayerEntity, Double, Boolean> clientAction) {
        super(essenceCost, cooldownTicks, learnLevelCost, action);
        this.serverAction = serverAction;
        this.clientAction = clientAction;
    }

}
