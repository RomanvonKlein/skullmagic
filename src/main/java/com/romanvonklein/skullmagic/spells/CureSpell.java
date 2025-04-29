package com.romanvonklein.skullmagic.spells;

import java.util.concurrent.ThreadLocalRandom;

import com.romanvonklein.skullmagic.mixin.ZombieVillagerEntityMixin;

import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class CureSpell extends Spell {

    protected CureSpell() {
        super(15000, 2400, 30, true);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        boolean success = false;
        double reachDistance = 5.0;
        Box box = player
                .getBoundingBox()
                .stretch(player.getRotationVec(1.0F).multiply(reachDistance))
                .expand(1.0D, 1.0D, 1.0D);
        Vec3d direction = player.getRotationVec(1);
        Vec3d cameraPos = player.getCameraPosVec(1);
        Vec3d vec3d3 = cameraPos.add(direction.multiply(reachDistance));
        EntityHitResult entityHitResult = ProjectileUtil.raycast(
                player,
                cameraPos,
                vec3d3,
                box,
                (entityx) -> !entityx.isSpectator() && entityx.canHit(),
                reachDistance * reachDistance);
        if (entityHitResult != null
                && entityHitResult.getEntity() instanceof ZombieVillagerEntity zombie) {
            ((ZombieVillagerEntityMixin) zombie).invokeSetConverting(player.getUuid(),
                    ThreadLocalRandom.current().nextInt(2401) + 3600);
            success = true;
        }
        return success;
    }

}
