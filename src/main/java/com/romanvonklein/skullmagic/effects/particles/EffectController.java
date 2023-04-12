package com.romanvonklein.skullmagic.effects.particles;

import com.romanvonklein.skullmagic.ClientInitializer;
import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.data.WorldBlockPos;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;

public class EffectController {
    private int sinceLastTick = 0;

    // Creates and moves particles to visualize links
    public void tickParticles(MinecraftClient client) {
        sinceLastTick++;
        if (sinceLastTick > 10) {
            sinceLastTick = 0;
            int spellParticles = 0;
            int skullparticles = 0;
            // spawn a particle for each pedestal, flying towards the linked altar
            // SkullAltars
            if (client.world != null) {
                WorldBlockPos altarPos = ClientInitializer.getClientData().getActiveAltarWorldBlockPos();
                if (altarPos != null && client.world.getRegistryKey().toString().equals(altarPos.worldKey.toString())) {
                    for (BlockPos pedPos : ClientInitializer.getClientData()
                            .getConnectedSkullPedestals()) {
                        Vec3f velocity = new Vec3f(altarPos.getX() - pedPos.getX(), altarPos.getY() - pedPos.getY(),
                                altarPos.getZ() - pedPos.getZ());
                        velocity.scale(0.1f);
                        client.world.addParticle(SkullMagic.LINK_PARTICLE, true, pedPos.getX() + 0.5,
                                pedPos.getY() + 0.5,
                                pedPos.getZ() + 0.5, velocity.getX(),
                                velocity.getY(), velocity.getZ());
                        skullparticles++;
                    }
                }

                // SpellAltars
                for (WorldBlockPos shrinePos : ClientInitializer.getClientData().getActiveSpellShrinesWorldBlockPos()) {
                    if (client.world.getRegistryKey().toString().equals(shrinePos.worldKey.toString())) {
                        for (BlockPos pedPos : ClientInitializer.getClientData()
                                .getSpellPedestalsForSpellAltar(shrinePos)) {
                            Vec3f velocity = new Vec3f(shrinePos.getX()-pedPos.getX(),
                                    shrinePos.getY()-pedPos.getY(),
                                    shrinePos.getZ()-pedPos.getZ());
                            velocity.scale(0.1f);
                            client.world.addParticle(SkullMagic.LINK_PARTICLE, true, pedPos.getX() + 0.5,
                                    pedPos.getY() + 0.5,
                                    pedPos.getZ() + 0.5, velocity.getX(),
                                    velocity.getY(), velocity.getZ());
                            spellParticles++;
                        }
                    }
                }
                SkullMagic.LOGGER
                        .info("Spawned particles: spellshrines: " + spellParticles + " skullshrines: "
                                + skullparticles);
            }
        }

    }
}
