package com.romanvonklein.skullmagic.effects;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class Effects {
    public static final Effect POSITION_HEIGHLIGH_EFFECT = new Effect() {
        private static final int particlesPerTick = 1;

        @Override
        public void spawn(MinecraftClient client, String worldkey, Vec3d pos, double spellPower) {
            Random rand = Random.createLocal();
            for (int i = 0; i < particlesPerTick; i++) {
                client.world.addParticle(ParticleTypes.PORTAL, true, pos.x, pos.y, pos.z, rand.nextDouble() - 0.5,
                        rand.nextDouble() - 0.5, rand.nextDouble() - 0.5);
            }
        }

        @Override
        public void despawn(MinecraftClient client) {
        }

    };
    public static final Effect SPAWNER_FIRE_EFFECT = new Effect() {
        @Override
        public void spawn(MinecraftClient client, String worldkey, Vec3d pos, double spellPower) {
            Random rand = Random.createLocal();
            client.world.addParticle(ParticleTypes.FLAME, true, pos.x, pos.y, pos.z,
                    (rand.nextDouble() - 0.5) * 0.05,
                    (rand.nextDouble()) * 0.05, (rand.nextDouble() - 0.5) * 0.05);
        }

        @Override
        public void despawn(MinecraftClient client) {
        }
    };
}
