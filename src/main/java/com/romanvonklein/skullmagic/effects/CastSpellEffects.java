package com.romanvonklein.skullmagic.effects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class CastSpellEffects {
    private CastSpellEffects() {
    }

    private static final Map<String, ? extends Effect> SpellEffects = new HashMap<>();
    public static final ParticleEffect SIMPLE_PARTICLE_EFFECT = new ParticleEffect() {

        @Override
        public void spawn(MinecraftClient client, String worldkey, List<Vec3d> positions, double spellPower) {
            int numParticles = (int) Math.round(spellPower * 25.0);
            int numPathParticles = (int) Math.round(spellPower * 10.0);
            // casting position effect
            Vec3d castPos = positions.get(0);
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            for (int i = 0; i < numParticles; i++) {

                double vx = (rand.nextDouble() - 0.5) * spellPower;
                double vy = (rand.nextDouble() - 0.5) * spellPower;
                double vz = (rand.nextDouble() - 0.5) * spellPower;
                client.world.addParticle(SkullMagic.SLOWING_EFFECT_PARTICLE, true, castPos.x, castPos.y, castPos.z, vx,
                        vy, vz);

            }
            if (positions.size() > 1) {
                // effect on target
                Vec3d targetPos = positions.get(1);
                for (int i = 0; i < numParticles; i++) {
                    double vx = (rand.nextDouble() - 0.5) * spellPower;
                    double vy = (rand.nextDouble() - 0.5) * spellPower;
                    double vz = (rand.nextDouble() - 0.5) * spellPower;
                    client.world.addParticle(SkullMagic.SLOWING_EFFECT_PARTICLE, true, targetPos.x, targetPos.y,
                            targetPos.z,
                            vx,
                            vy, vz);
                }
                // effect on link
                for (int i = 0; i < numPathParticles; i++) {
                    double startX = (rand.nextDouble() * 2 - 0.5) + castPos.x;
                    double startY = (rand.nextDouble() * 2 - 0.5) + castPos.y;
                    double startZ = (rand.nextDouble() * 2 - 0.5) + castPos.z;
                    double speedMultiplier = rand.nextDouble() + 0.5;
                    double ovx = ((targetPos.x - startX) / 25.0) * speedMultiplier;
                    double ovy = ((targetPos.y - startY) / 25.0) * speedMultiplier;
                    double ovz = ((targetPos.z - startZ) / 25.0) * speedMultiplier;
                    client.world.addParticle(SkullMagic.SIMPLE_EFFECT_PARTICLE, true, startX, startY,
                            startZ, ovx,
                            ovy, ovz);
                }

            }
            client.world.playSound(castPos.x, castPos.y, castPos.z, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                    SoundCategory.PLAYERS, 1.0f, 1.0f,
                    true);
        }

        @Override
        public void despawn(MinecraftClient client) {
        }

    };

    public static void castSpellEffect(MinecraftClient client, String spellname, String worldkey, Vec3d pos,
            double spellPower) {
        if (worldkey.equals(client.world.getRegistryKey().toString())) {
            if (SpellEffects.containsKey(spellname)) {
                SpellEffects.get(spellname).spawn(client, worldkey, Arrays.asList(pos), spellPower);
            } else {
                SIMPLE_PARTICLE_EFFECT.spawn(client, worldkey, Arrays.asList(pos), spellPower);
            }
        }
    }

    public static void castTargetedSpellEffect(MinecraftClient client, String spellname, String worldkey, Vec3d castPos,
            Vec3d targetPos, double spellPower) {
        if (worldkey.equals(client.world.getRegistryKey().toString())) {
            if (SpellEffects.containsKey(spellname)) {
                SpellEffects.get(spellname).spawn(client, worldkey, Arrays.asList(castPos, targetPos), spellPower);
            } else {
                SIMPLE_PARTICLE_EFFECT.spawn(client, worldkey, Arrays.asList(castPos, targetPos), spellPower);
            }
        }
    }
}
