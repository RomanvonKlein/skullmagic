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
    private int power;

    public EffectBall(EntityType<? extends AbstractFireballEntity> type, World world) {
        super(type, world);
        this.radius = 3.0f;
        this.effect = StatusEffects.POISON;
    }

    private EffectBall(World world, LivingEntity owner, double velocityX, double velocityY, double velocityZ,
            StatusEffect effect, float radius, int power) {
        super((EntityType<? extends AbstractFireballEntity>) SkullMagic.EFFECT_BALL, owner, velocityX, velocityY,
                velocityZ, world);
        this.radius = radius;
        this.effect = effect;
        this.power = power;
    }

    public static EffectBall createEffectBall(World world, LivingEntity owner, double velocityX, double velocityY,
            double velocityZ,
            StatusEffect effect, float radius, int power) {
        return new EffectBall(world, owner, velocityX, velocityY, velocityZ, effect, radius, power);
    }

    public void setEffect(StatusEffect effect) {
        this.effect = effect;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        if (!this.world.isClient) {
            spawnLingeringEffect();
            this.discard();
        }
        super.onCollision(hitResult);
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
            areaEffectCloudEntity.addEffect(new StatusEffectInstance(effect, 400, this.power));
            this.world.spawnEntity(areaEffectCloudEntity);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putByte("radius", (byte) this.radius);
        nbt.putInt("statuseffect", StatusEffect.getRawId(this.effect));
        super.writeCustomDataToNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("radius")) {
            this.radius = nbt.getByte("radius");
        }
        if (nbt.contains("statuseffect")) {
            this.effect = StatusEffect.byRawId(nbt.getInt("statuseffect"));
        }
        super.readCustomDataFromNbt(nbt);
    }

}
