package com.romanvonklein.skullmagic.mixin;

import com.romanvonklein.skullmagic.SkullMagic;
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

import com.romanvonklein.skullmagic.blocks.SkullPedestal;

@Mixin(Block.class)
public class PlaceBlockMixin {
    @Inject(at = @At("HEAD"), method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", cancellable = true)
    private void restrict(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack,
            CallbackInfo info) {
        if (!world.isClient()) {
            SkullMagic.LOGGER.info("placement mixin called.");
            String blockIdentifier = Registry.BLOCK.getId(state.getBlock()).toString();
            if (Config.getConfig().skulls.containsKey(blockIdentifier)) {
                BlockPos below = pos.down();
                BlockState pedestalCandidateBlockState = world.getBlockState(below);
                String pedestalCandidateIdentifier = Registry.BLOCK.getId(pedestalCandidateBlockState.getBlock())
                        .toString();
                SkullMagic.LOGGER.info("Placed a skull: " + blockIdentifier + " onto: " + pedestalCandidateIdentifier);

                // TODO: smart better pedestal detection.

                if (pedestalCandidateIdentifier.equals("skullmagic:skull_pedestal")) {
                    SkullMagic.LOGGER.info("Skull Placed on pedestal!");
                    ((SkullPedestal) pedestalCandidateBlockState.getBlock()).addSkull(world, pos.down(),
                            blockIdentifier,
                            (PlayerEntity) placer);
                }
            }
        }
    }
}
