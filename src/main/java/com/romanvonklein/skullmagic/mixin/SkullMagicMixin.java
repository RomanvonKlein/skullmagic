package com.romanvonklein.skullmagic.mixin;

import com.romanvonklein.skullmagic.SkullMagic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(PlayerEntity.class)
public abstract class SkullMagicMixin extends EntityMixin {

	@Inject(at = @At("HEAD"), method = "onKilledOther(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V")
	private void onKilledOther(net.minecraft.server.world.ServerWorld world, net.minecraft.entity.LivingEntity other,
			CallbackInfo info) {
		SkullMagic.LOGGER.info("Player killed a thing(" + other.getName() + ")!");
		// PlayerEntity ent = world.getClosestPlayer(other, 5.0);
		world.spawnEntity(new ItemEntity(world, this.getPos().x, this.getPos().y, this.getPos().z,
				new ItemStack(Items.MELON_SLICE)));
	}
}
