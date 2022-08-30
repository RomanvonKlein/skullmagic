package com.romanvonklein.skullmagic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.essence.EssenceManager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Mixin(Block.class)
public class PlaceBlockMixin {
//onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
@Inject(method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V",at = @At("HEAD") )
    private void restrict(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack,
            CallbackInfo info) {
                //TODO: overwrite onPlaced instead for custom bocks/blockEntities
        if (!world.isClient()) {
            String blockIdentifier = Registry.BLOCK.getId(state.getBlock()).toString();
            // cases:
            if (Config.getConfig().skulls.containsKey(blockIdentifier)) {
                // skull placed on pedestal?
                SkullMagic.essenceManager.tryLinkSkullPedestalToNearbyAltar(world, pos.down());
            } else if (blockIdentifier
                    .equals(BlockEntityType.getId(SkullMagic.SKULL_PEDESTAL_BLOCK_ENTITY).toString())) {
                // pedestal placed under skull?
                SkullMagic.essenceManager.tryLinkSkullPedestalToNearbyAltar(world, pos);
            } else if (blockIdentifier.equals(BlockEntityType.getId(SkullMagic.SKULL_ALTAR_BLOCK_ENTITY).toString())) {
                // altar places around valid pedestal - skull combination?
                SkullMagic.essenceManager.createNewEssencePool(world, pos);
                SkullMagic.essenceManager.tryLinkNearbyUnlinkedPedestals(world, pos);
            }
        }
    }

    // @Inject(at = @At("HEAD"), method =
    // "onDestroyedByExplosion(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/explosion/Explosion;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V",
    // cancellable = true)
    // public void onDestroyedByExplosion(World world, BlockPos pos, Explosion
    // explosion, CallbackInfo info) {
    // /*
    // * TODO: make use of this
    // * SkullMagic.LOGGER.info(((BlockInvoker) this).toString() +
    // * " destroyed by explosion!");
    // */
    // }

    /*
     * public void onBreak(net.minecraft.world.World world,
     * net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state,
     * net.minecraft.entity.player.PlayerEntity player)
     */
    @Inject(at = @At("HEAD"), method = "onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V", cancellable = true)
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo info) {
        if (!world.isClient) {
            if (EssenceManager.isValidSkullPedestalCombo(world, pos.down())) {
                SkullMagic.essenceManager.removePedestal(world.getRegistryKey(), pos.down());
            }
        }
    }
}
