package com.romanvonklein.skullmagic.entities;

import java.io.IOException;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FireBreath extends AbstractFireballEntity {
    int burnDuration = 10;
    int lifeTime = 30;

    public FireBreath(EntityType<? extends AbstractFireballEntity> type, World world) {
        super(type, world);
    }

    private FireBreath(World world, LivingEntity owner, double x, double y, double z,
            int burnDuration, int lifeTime) {
        super((EntityType<? extends AbstractFireballEntity>) SkullMagic.FIRE_BREATH, owner, x, y,
                z, world);
        this.burnDuration = burnDuration;
        this.lifeTime = lifeTime;
    }

    public static FireBreath createFireBreath(World world, LivingEntity owner, double velocityX, double velocityY,
            double velocityZ, int burnDuration, int lifeTime) {
        return new FireBreath(world, owner, velocityX, velocityY, velocityZ, burnDuration, lifeTime);
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        World world = this.getWorld();
        Direction direction = blockHitResult.getSide();
        BlockPos pos = blockHitResult.getBlockPos().offset(direction);
        if (world.getBlockState(pos) == Blocks.AIR.getDefaultState()) {
            world.setBlockState(pos, Blocks.FIRE.getDefaultState());
        }
        this.discard();
        return;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        World world = this.getWorld();
        if (!world.isClient) {
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                this.onBlockHit((BlockHitResult) hitResult);
            } else if (hitResult.getType() == HitResult.Type.ENTITY) {
                this.onEntityHit((EntityHitResult) hitResult);
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        World world = this.getWorld();
        if (!world.isClient) {
            Entity ent = entityHitResult.getEntity();
            if (ent.getType() != SkullMagic.FIRE_BREATH) {
                entityHitResult.getEntity().setOnFireFor(this.burnDuration);
                this.discard();
            }
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        // add new fields to nbt
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("lifeTime", this.lifeTime);
        nbt.putInt("burnDuration", this.burnDuration);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("burnDuration")) {
            this.burnDuration = nbt.getInt("burnDuration");
        } else {
            this.burnDuration = 10;
        }
        if (nbt.contains("lifeTime")) {
            this.lifeTime = nbt.getInt("lifeTime");
        } else {
            this.lifeTime = 30;
        }

    }

    @Override
    public void tick() {
        if (this.lifeTime > 0) {
            this.lifeTime--;
        } else {
            this.discard();
            return;
        }
        HitResult hitResult;
        Entity entity = this.getOwner();
        if (!this.getWorld().isClient
                && (entity != null && entity.isRemoved() || !this.getWorld().isChunkLoaded(this.getBlockPos()))) {
            this.discard();
            return;
        }
        if (this.isBurning()) {
            this.setOnFireFor(1);
        }
        hitResult = ProjectileUtil.getCollision(this, this::canHit);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onCollision(hitResult);
        }
        this.checkBlockCollision();
        Vec3d vec3d = this.getVelocity();
        double d = this.getX() + vec3d.x;
        double e = this.getY() + vec3d.y;
        double f = this.getZ() + vec3d.z;
        ProjectileUtil.setRotationFromVelocity(this, 0.2f);
        float g = this.getDrag();
        if (this.isTouchingWater()) {
            for (int i = 0; i < 4; ++i) {
                float h = 0.25f;
                this.getWorld().addParticle(ParticleTypes.BUBBLE, d - vec3d.x * 0.25, e - vec3d.y * 0.25,
                        f - vec3d.z * 0.25, vec3d.x, vec3d.y, vec3d.z);
            }
            g = 0.8f;
        }
        this.setVelocity(vec3d.add(this.powerX, this.powerY, this.powerZ).multiply(g));
        this.getWorld().addParticle(this.getParticleType(), d, e + 0.5, f, 0.0, 0.0, 0.0);
        this.setPosition(d, e, f);
    }
}
