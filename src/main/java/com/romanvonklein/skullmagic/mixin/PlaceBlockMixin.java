package com.romanvonklein.skullmagic.mixin;

import java.util.ArrayList;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.SkullAltarBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SkullPedestalBlockEntity;
import com.romanvonklein.skullmagic.config.Config;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
                tryLinkNearbyUnlinkedPedestals(world, pos);
            }
        }
    }

    /*
     * public void onDestroyedByExplosion(net.minecraft.world.World world,
     * net.minecraft.util.math.BlockPos pos, net.minecraft.world.explosion.Explosion
     * explosion)
     */
    @Inject(at = @At("HEAD"), method = "onDestroyedByExplosion(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/explosion/Explosion;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", cancellable = true)
    public void onBreak(World world, BlockPos pos, Explosion explosion, CallbackInfo info) {
        SkullMagic.LOGGER.info(world.getBlockState(pos).getBlock() + " destroyed by explosion!");
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

    private void tryLinkNearbyUnlinkedPedestals(World world, BlockPos altarPos) {
        Optional<SkullAltarBlockEntity> altarOpt = world.getBlockEntity(altarPos, SkullMagic.SKULL_ALTAR_BLOCK_ENTITY);
        if (altarOpt.isPresent()) {
            int height = Config.getConfig().scanHeight;
            int width = Config.getConfig().scanWidth;
            ArrayList<BlockPos> foundPedestals = new ArrayList<>();
            for (int x = altarPos.getX() - width; x < altarPos.getX() + width; x++) {
                for (int y = altarPos.getY() - height; y < altarPos.getY() + height; y++) {
                    for (int z = altarPos.getZ() - width; z < altarPos.getZ() + width; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (isValidSkullPedestalCombo(world, pos)) {
                            foundPedestals.add(pos);
                        }
                    }
                }
            }
            SkullAltarBlockEntity altar = altarOpt.get();
            for (BlockPos foundPedestalPos : foundPedestals) {
                if (!SkullMagic.StateManager.isPedestalLinked(foundPedestalPos)) {
                    altar.tryAddPedestal(foundPedestalPos);
                }
            }
        }
    }

    private void tryLinkToNearbyAltar(World world, BlockPos pedestalpos) {
        if (isValidSkullPedestalCombo(world, pedestalpos)) {
            int height = Config.getConfig().scanHeight;
            int width = Config.getConfig().scanWidth;
            for (int x = pedestalpos.getX() - width; x <= pedestalpos.getX() + width; x++) {
                for (int y = pedestalpos.getY() - height; y <= pedestalpos.getY() + height; y++) {
                    for (int z = pedestalpos.getZ() - width; z <= pedestalpos.getZ() + width; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        Optional<SkullAltarBlockEntity> opt = world.getBlockEntity(pos,
                                SkullMagic.SKULL_ALTAR_BLOCK_ENTITY);
                        if (opt.isPresent()) {
                            opt.get().tryAddPedestal(pedestalpos);
                        }
                    }
                }
            }
        }
    }

    public boolean isValidSkullPedestalCombo(World world, BlockPos pedestalPos) {
        Optional<SkullPedestalBlockEntity> pedestalCandidate = world.getBlockEntity(pedestalPos,
                SkullMagic.SKULL_PEDESTAL_BLOCK_ENTITY);
        if (pedestalCandidate.isPresent()) {
            BlockState skullCandidate = world.getBlockState(pedestalPos.up());
            String blockIdentifier = Registry.BLOCK.getId(skullCandidate.getBlock()).toString();
            return Config.getConfig().skulls.containsKey(blockIdentifier);
        }
        return false;
    }
}
