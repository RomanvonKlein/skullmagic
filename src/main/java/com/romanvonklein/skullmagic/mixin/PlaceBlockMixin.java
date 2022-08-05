package com.romanvonklein.skullmagic.mixin;

import java.util.Optional;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.SkullPedestalBlockEntity;
import com.romanvonklein.skullmagic.config.Config;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Mixin(Block.class)
public class PlaceBlockMixin {
    @Inject(at = @At("HEAD"), method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", cancellable = true)
    private void restrict(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack,
            CallbackInfo info) {
        if (!world.isClient()) {
            SkullMagic.LOGGER.info("placement mixin called.");
            String blockIdentifier = Registry.BLOCK.getId(state.getBlock()).toString();
            SkullMagic.LOGGER.info("BLock Identifier:" + blockIdentifier);

            if (Config.getConfig().skulls.containsKey(blockIdentifier)) {
                SkullMagic.LOGGER.info(blockIdentifier + " is a valid skull");
                BlockPos below = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
                Optional<SkullPedestalBlockEntity> optional = world.getBlockEntity(below,
                        SkullMagic.SKULL_PEDESTAL_BLOCK_ENTITY);

                if (optional.isPresent()) {
                    String pedestalCandidateIdentifier = Registry.BLOCK
                            .getId(world.getBlockState(optional.get().getPos()).getBlock())
                            .toString();
                    if (pedestalCandidateIdentifier.equals("skullmagic:skull_pedestal")) {
                        SkullMagic.LOGGER.info("Skull Placed on pedestal!");
                        optional.get().addSkull(world, pos.down(),
                                blockIdentifier,
                                (PlayerEntity) placer);
                    } else {
                        SkullMagic.LOGGER.info("Skull Placed, but not on pedestal(2st check failed)!");
                    }
                } else {
                    SkullMagic.LOGGER.info(
                            "Skull Placed, but not on pedestal(pos: " + pos + ",down: " + below.toString() + ")!");
                }

                // TODO: smart better pedestal detection.

            } else {
                SkullMagic.LOGGER.info(blockIdentifier + " not a valid skull.");
            }
        }
    }
}
