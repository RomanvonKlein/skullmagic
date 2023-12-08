package com.romanvonklein.skullmagic.entities;

import java.io.IOException;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class WitherBreath extends FireBreath {
    int damage = 10;
    int witherDuration = 10;
    int lifeTime = 30;

    public WitherBreath(EntityType<? extends AbstractFireballEntity> type, World world) {
        super(type, world);
    }

    private WitherBreath(World world, LivingEntity owner, double x, double y, double z,
            int witherDuration, int lifeTime, int damage) {
        super((EntityType<? extends AbstractFireballEntity>) SkullMagic.WITHER_BREATH, world);
        this.damage = damage;
        this.witherDuration = witherDuration;
        this.lifeTime = lifeTime;
        this.setVelocity(x, y, z);
    }

    public static WitherBreath createWitherBreath(World world, LivingEntity owner, double velocityX, double velocityY,
            double velocityZ, int witherDuration, int lifeTime, int damage) {
        return new WitherBreath(world, owner, velocityX, velocityY, velocityZ, witherDuration, lifeTime, damage);
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        try {
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                this.onBlockHit((BlockHitResult) hitResult);
            } else if (hitResult.getType() == HitResult.Type.ENTITY) {
                this.onEntityHit((EntityHitResult) hitResult);
            }
        } catch (Exception e) {
            SkullMagic.LOGGER.error(e.getMessage());
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!this.getWorld().isClient) {
            DamageSource damageSrc = null;
            damageSrc = this.getWorld().getDamageSources().wither();
            if (damageSrc != null) {
                Entity ent = entityHitResult.getEntity();
                if (ent.getType() != SkullMagic.FIRE_BREATH) {
                    ent.damage(damageSrc, damage);
                    if (ent instanceof LivingEntity temp) {
                        temp.addStatusEffect(
                                new StatusEffectInstance(StatusEffects.WITHER, this.witherDuration, 1));
                    }
                    this.discard();
                }
            }
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
    }
}
