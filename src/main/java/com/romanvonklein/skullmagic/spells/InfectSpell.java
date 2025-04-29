package com.romanvonklein.skullmagic.spells;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity.ZombieData;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class InfectSpell extends Spell {

    protected InfectSpell() {
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
        if (entityHitResult != null && entityHitResult.getEntity() instanceof VillagerEntity villager) {
            ServerWorld world = (ServerWorld) player.getWorld();
            ZombieVillagerEntity zombieVillagerEntity = villager.convertTo(EntityType.ZOMBIE_VILLAGER,
                    false);

            villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
            zombieVillagerEntity.initialize(world,
                    world.getLocalDifficulty(zombieVillagerEntity.getBlockPos()),
                    SpawnReason.CONVERSION, new ZombieData(false, true), null);
            zombieVillagerEntity.setVillagerData(villager.getVillagerData());
            zombieVillagerEntity
                    .setGossipData(villager.getGossip().serialize(NbtOps.INSTANCE));
            zombieVillagerEntity.setOfferData(villager.getOffers().toNbt());
            zombieVillagerEntity.setXp(villager.getExperience());
            success = true;
        }
        return success;
    }

}
