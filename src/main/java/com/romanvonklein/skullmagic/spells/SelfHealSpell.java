package com.romanvonklein.skullmagic.spells;

import net.minecraft.server.network.ServerPlayerEntity;

public class SelfHealSpell extends Spell {
    public SelfHealSpell() {
        super(50000, 100, 15, false);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        player.heal(2.0f + (float) (2 * powerLevel));
        return true;
    }

}
