package com.romanvonklein.skullmagic.entities;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class EffectBall extends AbstractFireballEntity {
    private float radius;
    private StatusEffect effect;

    public EffectBall(EntityType<? extends AbstractFireballEntity> type, World world) {
        super(type, world);
        this.radius = 3.0f;
        this.effect = StatusEffects.POISON;
    }

    private EffectBall(World world, LivingEntity owner, double velocityX, double velocityY, double velocityZ,
            StatusEffect effect, float radius) {
        super((EntityType<? extends AbstractFireballEntity>) SkullMagic.EFFECT_BALL, owner, velocityX, velocityY,
                velocityZ, world);
        this.radius = radius;
        this.effect = effect;
    }

    public static EffectBall createEffectBall(World world, LivingEntity owner, double velocityX, double velocityY,
            double velocityZ,
            StatusEffect effect, float radius) {
        return new EffectBall(world, owner, velocityX, velocityY, velocityZ, effect, radius);
    }

    public void setEffect(StatusEffect effect) {
        this.effect = effect;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.world.isClient) {
            spawnLingeringEffect();
            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!this.world.isClient) {
            spawnLingeringEffect();
            this.discard();
        }
    }

    private void spawnLingeringEffect() {
        if (!this.world.isClient) {
            SkullMagic.LOGGER.info("Spawning lingering effect: " + this.effect.toString());
            AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(this.world, this.getX(),
                    this.getY() + 1.0f, this.getZ());
            Entity entity = this.getOwner();
            if (entity instanceof LivingEntity) {
                areaEffectCloudEntity.setOwner((LivingEntity) entity);
            }
            areaEffectCloudEntity.setRadius(this.radius);
            areaEffectCloudEntity.setRadiusOnUse(-0.5f);
            areaEffectCloudEntity.setWaitTime(0);
            areaEffectCloudEntity.setDuration(400);
            areaEffectCloudEntity
                    .setRadiusGrowth(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());
            areaEffectCloudEntity.addEffect(new StatusEffectInstance(effect, 400, 1));
            this.world.spawnEntity(areaEffectCloudEntity);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putByte("radius", (byte) this.radius);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("radius")) {
            this.radius = nbt.getByte("radius");
        }
    }

}
