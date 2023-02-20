package com.romanvonklein.skullmagic.effects.particles;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;

public class LinkingParticles implements ParticleEffect {

    @Override
    public ParticleType<?> getType() {
        return ParticleTypes.BLOCK_MARKER;
    }

    @Override
    public void write(PacketByteBuf var1) {
        // TODO Auto-generated method stub

    }


    @Override
    public String asString() {
        // TODO Auto-generated method stub
        return null;
    }

}
