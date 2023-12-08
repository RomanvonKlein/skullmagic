package com.romanvonklein.skullmagic.effects.particles;

import com.romanvonklein.skullmagic.ClientInitializer;
import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.data.WorldBlockPos;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class EffectController {
    private int sinceLastTick = 0;

    // Creates and moves particles to visualize links
    public void tickParticles(MinecraftClient client) {
        sinceLastTick++;
        if (sinceLastTick > 10) {
            sinceLastTick = 0;
            if (client.player != null && (client.player.getOffHandStack().getItem().equals(SkullMagic.SKULL_WAND)
                    || client.player.getMainHandStack().getItem().equals(SkullMagic.SKULL_WAND))) {

                // spawn a particle for each pedestal, flying towards the linked altar
                // SkullAltars
                if (client.world != null) {
                    WorldBlockPos altarPos = ClientInitializer.getClientData().getActiveAltarWorldBlockPos();
                    if (altarPos != null
                            && client.world.getRegistryKey().toString().equals(altarPos.worldKey.toString())) {
                        for (BlockPos pedPos : ClientInitializer.getClientData()
                                .getConnectedSkullPedestals()) {
                            if (Math.random() > 0.25) {
                                client.world.addParticle(SkullMagic.LINK_PARTICLE, true, pedPos.getX() + 0.5,
                                        pedPos.getY() + 0.5,
                                        pedPos.getZ() + 0.5, altarPos.getX() + 0.5, altarPos.getY() + 0.5,
                                        altarPos.getZ() + 0.5);
                            }
                        }
                    }

                    // SpellAltars
                    for (WorldBlockPos shrinePos : ClientInitializer.getClientData()
                            .getActiveSpellShrinesWorldBlockPos()) {
                        if (client.world.getRegistryKey().toString().equals(shrinePos.worldKey.toString())
                                && Math.random() > 0.25) {
                            for (BlockPos pedPos : ClientInitializer.getClientData()
                                    .getSpellPedestalsForSpellAltar(shrinePos)) {
                                if (Math.random() > 0.25) {
                                    client.world.addParticle(SkullMagic.LINK_PARTICLE, true, pedPos.getX() + 0.5,
                                            pedPos.getY() + 0.5,
                                            pedPos.getZ() + 0.5, shrinePos.getX() + 0.5, shrinePos.getY() + 0.5,
                                            shrinePos.getZ() + 0.5);
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
