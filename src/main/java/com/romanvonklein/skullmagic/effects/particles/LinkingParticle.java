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
import net.minecraft.util.math.Vec3d;

@Environment(value = EnvType.CLIENT)
public class LinkingParticle extends AbstractSlowingParticle {
    Vec3d targetBlockPos;

    protected LinkingParticle(ClientWorld clientWorld, double spawnPosX, double spawnPosY, double spawnPosZ,
            double targetPosX, double targetPosY, double targetPosZ) {
        super(clientWorld, spawnPosX, spawnPosY, spawnPosZ, 0.0, 1.0, 0.0);
        this.targetBlockPos = new Vec3d(targetPosX, targetPosY, targetPosZ);
        this.collidesWithWorld = false;
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
        // SkullMagic.LOGGER.info(this.x + "," + this.y + "," + this.z + " : " + x + ","
        // + y + "," + z);
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

        Vec3d prevPos = new Vec3d(this.prevPosX, this.prevPosY, this.prevPosZ);
        double absDiff = targetBlockPos.distanceTo(prevPos);
        Vec3d diffVec = this.targetBlockPos.subtract(prevPos);
        this.velocityX = 0.05 * diffVec.x;
        this.velocityY = 0.05 * diffVec.y;
        this.velocityZ = 0.05 * diffVec.z;
        if (absDiff <= 1.5) {
            // close enought to ascend
            this.velocityY = 0.05;
        }

        this.move(this.velocityX, this.velocityY, this.velocityZ);
        if (this.ascending && this.y == this.prevPosY) {// not sure i want this...
            this.velocityX *= 1.1;
            this.velocityZ *= 1.1;
        }

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
            LinkingParticle linkingParticle = new LinkingParticle(clientWorld, d, e, f, g, h, i);
            linkingParticle.setSprite(this.spriteProvider);
            return linkingParticle;
        }
    }
}
