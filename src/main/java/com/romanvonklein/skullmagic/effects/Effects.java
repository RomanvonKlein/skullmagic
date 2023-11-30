package com.romanvonklein.skullmagic.effects;

import java.util.List;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class Effects {
    public static final Effect POSITION_HEIGHLIGH_EFFECT = new Effect() {
        private static final int particlesPerTick = 1;

        @Override
        public void spawn(MinecraftClient client, String worldkey, List<Vec3d> positions, double spellPower) {
            Vec3d pos = positions.get(0);
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
        public void spawn(MinecraftClient client, String worldkey, List<Vec3d> positions, double spellPower) {
            Vec3d pos = positions.get(0);
            Random rand = Random.createLocal();
            client.world.addParticle(ParticleTypes.FLAME, true, pos.x, pos.y, pos.z, (rand.nextDouble() - 0.5) * 0.05,
                    (rand.nextDouble()) * 0.05, (rand.nextDouble() - 0.5) * 0.05);
        }

        @Override
        public void despawn(MinecraftClient client) {
        }
    };
    public static final Effect CONNECTING_EFFECT = new Effect() {
        @Override
        public void spawn(MinecraftClient client, String worldkey, List<Vec3d> positions, double spellPower) {
            Vec3d altarPos = positions.get(0);
            Vec3d targetPos = positions.get(1);
            Vec3d centerPos = altarPos.add(altarPos.subtract(targetPos).multiply(-0.5));
            int num_particles = 5;
            Vec3d moveVec = centerPos.subtract(altarPos).multiply(0.025);
            for (int i = 0; i < num_particles; i++) {
                Random rand = Random.createLocal();
                Vec3d moddedMoveVec = moveVec.multiply(rand.nextDouble() + 0.5);
                client.world.addParticle(SkullMagic.CONNECTING_EFFECT_PARTICLE, true, altarPos.x, altarPos.y,
                        altarPos.z, moddedMoveVec.x, moddedMoveVec.y, moddedMoveVec.z);
            }
            for (int i = 0; i < num_particles; i++) {
                Random rand = Random.createLocal();
                Vec3d moddedMoveVec = moveVec.multiply(rand.nextDouble() + 0.5);
                client.world.addParticle(SkullMagic.CONNECTING_EFFECT_PARTICLE, true, targetPos.x, targetPos.y,
                        targetPos.z, -moddedMoveVec.x, -moddedMoveVec.y, -moddedMoveVec.z);
            }
            for (int i = 0; i < num_particles; i++) {
                Random rand = Random.createLocal();
                Vec3d moddedMoveVec = new Vec3d((rand.nextDouble() - 0.5), (rand.nextDouble() - 0.5),
                        (rand.nextDouble() - 0.5)).multiply(0.01);
                client.world.addParticle(SkullMagic.CONNECTING_EFFECT_PARTICLE, true, centerPos.x, centerPos.y,
                        centerPos.z, -moddedMoveVec.x, -moddedMoveVec.y, -moddedMoveVec.z);
            }
        }

        @Override
        public void despawn(MinecraftClient client) {
        }
    };
    public static final Effect DISCONNECTING_EFFECT = new Effect() {
        @Override
        public void spawn(MinecraftClient client, String worldkey, List<Vec3d> positions, double spellPower) {
            Vec3d altarPos = positions.get(0);
            Vec3d targetPos = positions.get(1);
            Vec3d centerPos = altarPos.add(altarPos.subtract(targetPos).multiply(-0.5));
            int num_particles = 5;
            Vec3d moveVec = centerPos.subtract(altarPos).multiply(0.025);
            for (int i = 0; i < num_particles; i++) {
                Random rand = Random.createLocal();
                Vec3d moddedMoveVec = moveVec.multiply(rand.nextDouble() + 0.5);
                client.world.addParticle(SkullMagic.CONNECTING_EFFECT_PARTICLE, true, centerPos.x, centerPos.y,
                        centerPos.z, moddedMoveVec.x, moddedMoveVec.y, moddedMoveVec.z);
            }
            for (int i = 0; i < num_particles; i++) {
                Random rand = Random.createLocal();
                Vec3d moddedMoveVec = moveVec.multiply(rand.nextDouble() + 0.5);
                client.world.addParticle(SkullMagic.CONNECTING_EFFECT_PARTICLE, true, centerPos.x, centerPos.y,
                        centerPos.z, -moddedMoveVec.x, -moddedMoveVec.y, -moddedMoveVec.z);
            }
            for (int i = 0; i < num_particles; i++) {
                Random rand = Random.createLocal();
                Vec3d moddedMoveVec = new Vec3d((rand.nextDouble() - 0.5), (rand.nextDouble() - 0.5),
                        (rand.nextDouble() - 0.5)).multiply(0.01);
                client.world.addParticle(SkullMagic.DISCONNECTING_EFFECT_PARTICLE, true, centerPos.x, centerPos.y,
                        centerPos.z, -moddedMoveVec.x, -moddedMoveVec.y, -moddedMoveVec.z);
            }
        }

        @Override
        public void despawn(MinecraftClient client) {
        }
    };
}
