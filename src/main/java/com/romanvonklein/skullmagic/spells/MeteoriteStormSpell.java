package com.romanvonklein.skullmagic.spells;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.function.TriFunction;
import org.joml.Vector3f;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.tasks.DelayedTask;

import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class MeteoriteStormSpell extends Spell {
    public MeteoriteStormSpell() {
        super(65000, 600, 45, true);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        int meteoriteCount = 10 + (int) Math.round(2.0 * powerLevel);
        int minPower = 1;
        int maxPower = 5;
        int height = 256;
        int radius = 7;
        int maxDelay = 40;
        HitResult result = player.raycast(100, 1, false);
        if (result != null) {
            Vec3d center = result.getPos();
            Vector3f angle = Direction.DOWN.getUnitVector();
            Random rand = Random.create();

            World world = player.getWorld();
            for (int i = 0; i < meteoriteCount; i++) {
                DelayedTask tsk = new DelayedTask("meteoritestorm_spell_spawn_meteorites",
                        rand.nextInt(maxDelay),
                        new TriFunction<Object[], MinecraftServer, UUID, Boolean>() {
                            @Override
                            public Boolean apply(Object[] data, MinecraftServer server,
                                    UUID playerID) {
                                ServerPlayerEntity taskPlayerEnt = server.getPlayerManager()
                                        .getPlayer(playerID);
                                ThreadLocalRandom newRand = ThreadLocalRandom.current();
                                FireballEntity ent = new FireballEntity(world, taskPlayerEnt, angle.x(),
                                        angle.y(), angle.z(),
                                        (int) Math.round(
                                                Math.max(1.0,
                                                        Math.min(
                                                                minPower + newRand
                                                                        .nextInt(maxPower - minPower)
                                                                        + (powerLevel - 1) * 0.5,
                                                                5.0))));
                                ent.setPos(center.x - radius + 2 * newRand.nextFloat() * radius,
                                        height, center.z - radius + 2 * newRand.nextFloat() * radius);
                                ent.setVelocity(0, -15, 0);
                                world.spawnEntity(ent);
                                return true;
                            }
                        }, null, player.getUuid());
                SkullMagic.taskManager.queueTask(tsk);
            }
        }
        return true;
    }
}
