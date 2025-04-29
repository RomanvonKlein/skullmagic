package com.romanvonklein.skullmagic.spells;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

public class StrengthBuffSpell extends Spell {

    protected StrengthBuffSpell() {
        super(7500, 150, 15, false);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH,
                (int) Math.round(500 * (1 + (powerLevel - 1) * 0.25)),
                (int) Math.round(Math.max(0.0, (powerLevel - 1) / 2.0))));
        return true;
    }

}