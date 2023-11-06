package com.romanvonklein.skullmagic.entities;

import java.io.IOException;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
        try (World world = this.getWorld()) {
            Direction direction = blockHitResult.getSide();
            BlockPos pos = blockHitResult.getBlockPos().offset(direction);
            if (world.getBlockState(pos) == Blocks.AIR.getDefaultState()) {
                world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            }
            this.discard();
        } catch (IOException e) {
            SkullMagic.LOGGER.error(e.getMessage());
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        try (World world = this.getWorld()) {
            if (!world.isClient) {
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    this.onBlockHit((BlockHitResult) hitResult);
                } else if (hitResult.getType() == HitResult.Type.ENTITY) {
                    this.onEntityHit((EntityHitResult) hitResult);
                }
            }
        } catch (IOException e) {
            SkullMagic.LOGGER.error(e.getMessage());
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        try (World world = this.getWorld()) {
            if (!world.isClient) {
                Entity ent = entityHitResult.getEntity();
                if (ent.getType() != SkullMagic.FIRE_BREATH) {
                    entityHitResult.getEntity().setOnFireFor(this.burnDuration);
                    this.discard();
                }
            }
        } catch (IOException e) {
            SkullMagic.LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
    }

    @Override
    public void tick() {
        if (this.lifeTime > 0) {
            this.lifeTime--;
        } else {
            this.discard();
        }
        super.tick();

    }
}
