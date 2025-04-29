package com.romanvonklein.skullmagic.spells;

import com.romanvonklein.skullmagic.entities.EffectBall;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PoisonBallSpell extends Spell {

    protected PoisonBallSpell() {
        super(5000, 150, 10, false);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        World world = player.getWorld();
        if (!world.isClient) {
            Vec3d velocity = player.getRotationVector().multiply(8.0 + powerLevel * 2);
            EffectBall ball = EffectBall.createEffectBall(world, player, velocity.x, velocity.y,
                    velocity.z,
                    StatusEffects.POISON, 4.0f,
                    (int) Math.round(Math.max(1.0, powerLevel / 3)));
            ball.setPosition(
                    player.getCameraEntity().getPos().add(player.getRotationVector().normalize()));
            world.spawnEntity(ball);
        }
        return true;
    }

}
