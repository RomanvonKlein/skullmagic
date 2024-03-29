package com.romanvonklein.skullmagic.effects.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.AbstractSlowingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.random.Random;

@Environment(value = EnvType.CLIENT)
public class SlowingEffectParticle extends AbstractSlowingParticle {

    protected SlowingEffectParticle(ClientWorld clientWorld, double spawnPosX, double spawnPosY, double spawnPosZ,
            double speedX, double speedY, double speedZ) {
        super(clientWorld, spawnPosX, spawnPosY, spawnPosZ, speedX, speedY, speedZ);
        this.collidesWithWorld = true;
        Random rand = Random.createLocal();

        this.maxAge = rand.nextBetween(30, 70);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getSize(float tickDelta) {
        return 0.5f * ((this.maxAge - this.age) / 70.0f);
    }

    @Override
    public void move(double x, double y, double z) {
        this.setPos(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        this.move(this.velocityX, this.velocityY, this.velocityZ);

        this.velocityX *= 0.9;
        this.velocityY *= 0.9;
        this.velocityZ *= 0.9;
    }

    @Environment(value = EnvType.CLIENT)
    public static class Factory
            implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d,
                double e, double f, double g, double h, double i) {
            SlowingEffectParticle effectParticle = new SlowingEffectParticle(clientWorld, d, e, f, g, h, i);
            effectParticle.setSprite(this.spriteProvider);
            return effectParticle;
        }
    }
}
