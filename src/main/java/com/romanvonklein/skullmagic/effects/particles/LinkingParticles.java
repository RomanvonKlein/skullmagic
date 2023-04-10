package com.romanvonklein.skullmagic.effects.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.BlockPos;

@Environment(value = EnvType.CLIENT)
public class LinkingParticles extends DefaultParticleType {
    BlockPos targetBlockPos;

    protected LinkingParticles(boolean alwaysShow, BlockPos target) {
        super(alwaysShow);
        this.targetBlockPos = target;
    }

}
