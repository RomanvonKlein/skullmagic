package com.romanvonklein.skullmagic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.persistantState.EssenceManager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

@Mixin(Block.class)
public class PlaceBlockMixin {

    @Inject(at = @At("HEAD"), method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", cancellable = true)
    private void restrict(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack,
            CallbackInfo info) {
        if (!world.isClient()) {
            String blockIdentifier = Registry.BLOCK.getId(state.getBlock()).toString();
            // cases:
            if (Config.getConfig().skulls.containsKey(blockIdentifier)) {
                // skull placed on pedestal?
                SkullMagic.LOGGER.info("placed a skull");
                tryLinkToNearbyAltar(world, pos.down());
            } else if (blockIdentifier
                    .equals(BlockEntityType.getId(SkullMagic.SKULL_PEDESTAL_BLOCK_ENTITY).toString())) {
                // pedestal placed under skull?
                tryLinkToNearbyAltar(world, pos);
                SkullMagic.LOGGER.info("placed a skull pedestal");
            } else if (blockIdentifier.equals(BlockEntityType.getId(SkullMagic.SKULL_ALTAR_BLOCK_ENTITY).toString())) {
                SkullMagic.LOGGER.info("placed a skull altar");
                // altar places around valid pedestal - skull combination?
                SkullMagic.essenceManager.createNewEssencePool(world, pos);   
                SkullMagic.essenceManager.tryLinkNearbyUnlinkedPedestals(world, pos);
            }
        }
    }

    /*
     * public void onDestroyedByExplosion(net.minecraft.world.World world,
     * net.minecraft.util.math.BlockPos pos, net.minecraft.world.explosion.Explosion
     * explosion)
     */
    @Inject(at = @At("HEAD"), method = "onDestroyedByExplosion(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/explosion/Explosion;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", cancellable = true)
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion, CallbackInfo info) {
        /*
         * TODO: make use of this
         * SkullMagic.LOGGER.info(((BlockInvoker) this).toString() +
         * " destroyed by explosion!");
         */
    }

    /*
     * public void onBreak(net.minecraft.world.World world,
     * net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state,
     * net.minecraft.entity.player.PlayerEntity player)
     */
    // @Inject(at = @At("HEAD"), method =
    // "onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V",
    // cancellable = true)
    // public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity
    // player, CallbackInfo info) {
    // SkullMagic.LOGGER.info("BLock Broken!");
    // }

    // TODO: this method should propably be in EssenceManager.
    private void tryLinkToNearbyAltar(World world, BlockPos pedestalpos) {
        if (EssenceManager.isValidSkullPedestalCombo(world, pedestalpos)) {
            String skullCandidate = Registry.BLOCK
                    .getId(world.getBlockState(pedestalpos.up()).getBlock())
                    .toString();
            int height = Config.getConfig().scanHeight;
            int width = Config.getConfig().scanWidth;
            for (int x = pedestalpos.getX() - width; x <= pedestalpos.getX() + width; x++) {
                for (int y = pedestalpos.getY() - height; y <= pedestalpos.getY() + height; y++) {
                    for (int z = pedestalpos.getZ() - width; z <= pedestalpos.getZ() + width; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        SkullMagic.essenceManager.linkPedestalToEssencePool(world.getRegistryKey(), pedestalpos, pos,
                                skullCandidate);
                    }
                }
            }
        }
    }

}
