package com.romanvonklein.skullmagic.spells;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.function.TriFunction;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.tasks.DelayedTask;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LightningStormSpell extends Spell {

    protected LightningStormSpell() {
        super(50000, 450, 40, true);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        int lightningCount = (int) Math.round(Math.min(10 + powerLevel * 3, 50));
        int radius = 7;
        int maxDelay = 40;
        HitResult result = player.raycast(100, 1, false);
        if (result != null) {
            Vec3d center = result.getPos();
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            for (int i = 0; i < lightningCount; i++) {
                DelayedTask tsk = new DelayedTask("meteoritestorm_spell_spawn_meteorites",
                        rand.nextInt(maxDelay),
                        new TriFunction<Object[], MinecraftServer, UUID, Boolean>() {
                            @Override
                            public Boolean apply(Object[] data, MinecraftServer server,
                                    UUID playerID) {
                                ServerPlayerEntity taskPlayerEnt = server.getPlayerManager()
                                        .getPlayer(playerID);
                                ThreadLocalRandom newRand = ThreadLocalRandom.current();

                                World world = taskPlayerEnt.getWorld();
                                LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT,
                                        world);
                                bolt.setPos(center.x - radius + 2 * newRand.nextFloat() * radius,
                                        center.y, center.z - radius + 2 * newRand.nextFloat() * radius);
                                bolt.setVelocity(0, -15, 0);
                                world.spawnEntity(bolt);
                                return true;
                            }
                        }, null, player.getUuid());
                SkullMagic.taskManager.queueTask(tsk);
            }
        }
        return true;
    }

}
