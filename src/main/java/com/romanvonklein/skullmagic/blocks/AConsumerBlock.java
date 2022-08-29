package com.romanvonklein.skullmagic.blocks;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AConsumerBlock extends BlockWithEntity {

    protected AConsumerBlock(Settings settings) {
        super(settings);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (!world.isClient) {
            SkullMagic.essenceManager.removeConsumer(world.getRegistryKey(), pos);
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            SkullMagic.essenceManager.addConsumer(world.getRegistryKey(), pos, placer.getUuid());
        }
    }
}
