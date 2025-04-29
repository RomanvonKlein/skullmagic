package com.romanvonklein.skullmagic.spells;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.function.TriFunction;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.tasks.DelayedTask;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class WolfPackSpell extends Spell {
    public WolfPackSpell() {
        super(25000, 500, 25, false);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        int wolfCount = 2 + (int) Math.round((powerLevel - 1));
        int wolfLifeTime = 20 * 60;// ~one minute of lifetime
        ArrayList<WolfEntity> wolfesSpawned = new ArrayList<>();
        for (int i = 0; i < wolfCount; i++) {
            World world = player.getWorld();
            WolfEntity wolf = new WolfEntity(EntityType.WOLF, world);

            world.spawnEntity(wolf);
            wolf.setTamed(true);
            wolf.setOwner(player);
            wolf.setPosition(player.getPos());
            wolfesSpawned.add(wolf);
        }
        SkullMagic.taskManager.queueTask(new DelayedTask("wolfpack_spell_kill_wolfes", wolfLifeTime,
                new TriFunction<Object[], MinecraftServer, UUID, Boolean>() {
                    @Override
                    public Boolean apply(Object[] data, MinecraftServer server, UUID playerID) {
                        @SuppressWarnings("unchecked")
                        ArrayList<WolfEntity> wolfes = (ArrayList<WolfEntity>) data[0];
                        for (WolfEntity wolf : wolfes) {
                            if (wolf.isAlive()) {
                                wolf.kill();
                            }
                        }

                        return true;
                    }
                },
                new Object[] { wolfesSpawned }, player.getUuid()));
        return true;
    }
}
