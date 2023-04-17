package com.romanvonklein.skullmagic.mixin;

import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.util.Util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(Block.class)
public class PlaceBlockMixin {
    // onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity
    // placer, ItemStack itemStack)
    @Inject(method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
    private void restrict(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack,
            CallbackInfo info) {
        if (!world.isClient()) {
            String blockIdentifier = Registries.BLOCK.getId(state.getBlock()).toString();
            // cases:
            if (Config.getConfig().skulls.containsKey(blockIdentifier)) {
                // skull placed on pedestal?
                SkullMagic.getServerData().tryLinkSkullPedestalToNearbyAltar((ServerWorld) world, pos.down());
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V", cancellable = true)
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo info) {
        if (!world.isClient) {
            if (Util.getPedestalSkullIdentifier(world, pos.down()) != null) {
                SkullMagic.getServerData().removePedestal((ServerWorld) world, pos.down());
            }
        }
    }
}
