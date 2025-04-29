package com.romanvonklein.skullmagic.spells;

import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FireBallSpell extends Spell {

    public FireBallSpell() {
        super(100000, 100, 15, false);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        Vec3d angle = player.getRotationVector().normalize()
                .multiply(powerLevel / 2 + 1.5);
        Vec3d pos = player.getPos();
        World world = player.getWorld();
        FireballEntity ent = new FireballEntity(world, player,
                angle.getX(),
                angle.getY(),
                angle.getZ(),
                Math.max(1, Math.min((int) Math.round(powerLevel), 5)));
        ent.setPos(pos.x, pos.y + player.getHeight(), pos.z);
        world.spawnEntity(ent);
        return true;
    }
}
