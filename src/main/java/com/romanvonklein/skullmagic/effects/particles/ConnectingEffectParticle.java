package com.romanvonklein.skullmagic.effects.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

@Environment(value = EnvType.CLIENT)
public class ConnectingEffectParticle extends SpriteBillboardParticle {

    protected ConnectingEffectParticle(ClientWorld clientWorld, double spawnPosX, double spawnPosY, double spawnPosZ,
            double speedX, double speedY, double speedZ) {
        super(clientWorld, spawnPosX, spawnPosY, spawnPosZ, speedX, speedY, speedZ);
        this.velocityX = speedX;
        this.velocityY = speedY;
        this.velocityZ = speedZ;
        this.collidesWithWorld = true;
        this.maxAge = 30;
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
            ConnectingEffectParticle effectParticle = new ConnectingEffectParticle(clientWorld, d, e, f, g, h, i);
            effectParticle.setSprite(this.spriteProvider);
            return effectParticle;
        }
    }
}
