package com.romanvonklein.skullmagic.spells;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class LungeSpell extends Spell {

    protected LungeSpell() {
        super(5000, 150, 5, false);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        if (!player.getWorld().isClient) {
            Vec3d velocity = player.getRotationVector().multiply(4.0 + powerLevel * 2);
            player.addVelocity(velocity.x, velocity.y, velocity.z);
            player.velocityModified = true;
        }
        return true;
    }

}