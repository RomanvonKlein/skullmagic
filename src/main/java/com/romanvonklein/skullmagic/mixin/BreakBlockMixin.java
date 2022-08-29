package com.romanvonklein.skullmagic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.persistantState.EssenceManager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(Block.class)
public class BreakBlockMixin {

    @Inject(at = @At("HEAD"), method = "onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", cancellable = true)
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo info) {
        if (!world.isClient) {
            if (EssenceManager.isValidSkullPedestalCombo(world, pos.down())) {
                SkullMagic.essenceManager.removePedestal(world.getRegistryKey(), pos.down());
            }
        }
    }
}
