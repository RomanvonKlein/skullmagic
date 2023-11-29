package com.romanvonklein.skullmagic.effects;

import java.util.HashMap;
import java.util.Map;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class CastSpellEffects {
    private CastSpellEffects() {
    }

    private static final Map<String, ? extends Effect> SpellEffects = new HashMap<>();
    public static final ParticleEffect SIMPLE_PARTICLE_EFFECT = new ParticleEffect() {

        @Override
        public void spawn(MinecraftClient client, String worldkey, Vec3d pos, double spellPower) {
            int numParticles = (int) Math.round(spellPower * 25.0);
            for (int i = 0; i < numParticles; i++) {
                Random rand = Random.createLocal();
                double vx = (rand.nextDouble() - 0.5) * spellPower;
                double vy = (rand.nextDouble() - 0.5) * spellPower;
                double vz = (rand.nextDouble() - 0.5) * spellPower;
                client.world.addParticle(SkullMagic.SIMPLE_EFFECT_PARTICLE, true, pos.x, pos.y, pos.z, vx, vy, vz);
                client.world.playSound(pos.x, pos.y, pos.z, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                        SoundCategory.PLAYERS, 1.0f, 1.0f,
                        true);
            }
        }

        @Override
        public void despawn(MinecraftClient client) {
        }

    };

    public static void castSpellEffect(MinecraftClient client, String spellname, String worldkey, Vec3d pos,
            double spellPower) {
        if (worldkey.equals(client.world.getRegistryKey().toString())) {
            if (SpellEffects.containsKey(spellname)) {
                SpellEffects.get(spellname).spawn(client, worldkey, pos, spellPower);
            } else {
                SIMPLE_PARTICLE_EFFECT.spawn(client, worldkey, pos, spellPower);
            }
        }
    }
}
