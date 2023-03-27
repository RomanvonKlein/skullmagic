package com.romanvonklein.skullmagic.blocks;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public abstract class AConsumerBlock extends BlockWithEntity {

    protected AConsumerBlock(Settings settings) {
        super(settings);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            SkullMagic.getServerData().removeConsumer(world.getRegistryKey(), pos);
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient) {
            if ((placer instanceof ServerPlayerEntity) && SkullMagic.getServerData().addConsumer(world.getRegistryKey(),
                    pos, ((ServerPlayerEntity) placer).getGameProfile().getId())) {
                world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE,
                        SoundCategory.BLOCKS, 1.0f, 1.0f, true);
            } else {
                world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_BEACON_DEACTIVATE,
                        SoundCategory.BLOCKS, 1.0f, 1.0f, true);
            }
        }
        super.onPlaced(world, pos, state, placer, itemStack);
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        if (!world.isClient) {
            SkullMagic.getServerData().removeConsumer(world.getRegistryKey(), pos);
        }
        super.onDestroyedByExplosion(world, pos, explosion);
    }
}
