package com.romanvonklein.skullmagic.spells;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.function.TriFunction;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.entities.FireBreath;
import com.romanvonklein.skullmagic.tasks.DelayedTask;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FireBreathSpell extends Spell {

    public FireBreathSpell(){
        super(5000, 150, 15, false);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        int shotsPerTick = 2;
        int tickDuration = 30;
        int breathLife = 20 + (int) Math.round(powerLevel * 4);
        int burnDuration = 40 + (int) Math.round(powerLevel * 10);
        for (int i = 0; i < tickDuration; i++) {// TODO: making this one single task may make it more
                                                // memory
                                                // efficient.
            SkullMagic.taskManager.queueTask(new DelayedTask("spawn_fire_breath_task", i,
                    new TriFunction<Object[], MinecraftServer, UUID, Boolean>() {
                        @Override
                        public Boolean apply(Object[] data, MinecraftServer server, UUID playerID) {
                            int shotsPerTick = ((int[]) data[0])[0];
                            int breathLife = ((int[]) data[0])[1];
                            int burnDuration = ((int[]) data[0])[2];
                            ThreadLocalRandom rand = ThreadLocalRandom.current();

                            ServerPlayerEntity taskPlayerEnt = server.getPlayerManager()
                                    .getPlayer(playerID);
                            Vec3d dir = taskPlayerEnt.getRotationVector().normalize();

                            World world = taskPlayerEnt.getWorld();
                            world.playSound(null, taskPlayerEnt.getBlockPos(),
                                    SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.BLOCKS, 1f, 1f);

                            for (int j = 0; j < shotsPerTick; j++) {
                                FireBreath entity = FireBreath.createFireBreath(world, taskPlayerEnt,
                                        dir.x + rand.nextFloat() - 0.5,
                                        dir.y + rand.nextFloat() - 0.5, dir.z + rand.nextFloat() - 0.5,
                                        burnDuration, breathLife);
                                entity.setPosition(
                                        taskPlayerEnt.getPos().add(dir.multiply(0.5))
                                                .add(0, taskPlayerEnt
                                                        .getEyeHeight(taskPlayerEnt.getPose()), 0));
                                world.spawnEntity(entity);

                            }

                            return true;
                        }
                    },
                    new Object[] {
                            new int[] { shotsPerTick, breathLife, burnDuration, i, tickDuration } },
                    player.getUuid()));
        }
        return true;
    }

}
