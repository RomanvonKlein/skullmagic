package com.romanvonklein.skullmagic.spells;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.function.TriFunction;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.entities.WitherBreath;
import com.romanvonklein.skullmagic.tasks.DelayedTask;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WitherBreathSpell extends Spell {
    public WitherBreathSpell() {
        super(7500, 150, 25, false);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        int shotsPerTick = 2 + (int) Math.floor(powerLevel / 2);
        int tickDuration = 30;
        int breathLife = 20 + (int) Math.round(powerLevel * 10);
        int witherDuration = 120 + (int) Math.round(powerLevel * 80);
        int damage = 1 + (int) Math.round(powerLevel * 2);
        for (int i = 0; i < tickDuration; i++) {// TODO: making this one single task may make it more
                                                // memory
                                                // efficient.
            SkullMagic.taskManager.queueTask(new DelayedTask("spawn_wither_breath_task", i,
                    new TriFunction<Object[], MinecraftServer, UUID, Boolean>() {
                        @Override
                        public Boolean apply(Object[] data, MinecraftServer server, UUID playerID) {
                            ServerPlayerEntity taskPlayerEnt = server.getPlayerManager()
                                    .getPlayer(playerID);
                            if (taskPlayerEnt != null) {
                                int shotsPerTick = ((int[]) data[0])[0];
                                int breathLife = ((int[]) data[0])[1];
                                int witherDuration = ((int[]) data[0])[2];
                                int damage = ((int[]) data[0])[3];
                                ThreadLocalRandom rand = ThreadLocalRandom.current();
                                Vec3d dir = taskPlayerEnt.getRotationVector().normalize();

                                World world = taskPlayerEnt.getWorld();
                                world.playSound(null, taskPlayerEnt.getBlockPos(),
                                        SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.BLOCKS, 1f, 1f);
                                for (int j = 0; j < shotsPerTick; j++) {
                                    WitherBreath entity = WitherBreath.createWitherBreath(world,
                                            taskPlayerEnt,
                                            dir.x + (rand.nextFloat() - 0.5),
                                            dir.y + (rand.nextFloat() - 0.5),
                                            dir.z + (rand.nextFloat() - 0.5),
                                            witherDuration, breathLife, damage);
                                    entity.setPosition(
                                            taskPlayerEnt.getPos().add(dir.multiply(0.5))
                                                    .add(0, taskPlayerEnt
                                                            .getEyeHeight(taskPlayerEnt.getPose()), 0));
                                    world.spawnEntity(entity);
                                }
                                return true;
                            } else {
                                SkullMagic.LOGGER
                                        .warn("Tried to execute Task " + "spawn_wither_breath_task"
                                                + " for non existing player.");
                                return false;
                            }
                        }
                    },
                    new Object[] { new int[] { shotsPerTick, breathLife, witherDuration, damage } },
                    player.getUuid()));
        }
        return true;
    }
}
