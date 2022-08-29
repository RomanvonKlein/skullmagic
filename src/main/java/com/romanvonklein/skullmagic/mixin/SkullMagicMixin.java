package com.romanvonklein.skullmagic.mixin;

import java.util.Map;
import java.util.Random;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Mixin(PlayerEntity.class)
public abstract class SkullMagicMixin extends EntityMixin {

	@Inject(at = @At("HEAD"), method = "onKilledOther(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V")
	private void onKilledOther(net.minecraft.server.world.ServerWorld world, net.minecraft.entity.LivingEntity other,
			CallbackInfoReturnable info) {
		String killedType = EntityType.getId(other.getType()).toString();
		if (Config.getConfig().drops.containsKey(killedType)) {
			Map<String, Float> itemDrops = Config.getConfig().drops.get(killedType);
			Random rand = new Random();
			for (String itemIdentifyer : itemDrops.keySet()) {
				Float roll = rand.nextFloat();
				Float chance = itemDrops.get(itemIdentifyer);
				if (roll <= chance) {
					Identifier itemId = Identifier.tryParse(itemIdentifyer);
					if (itemId != null) {
						other.dropStack(new ItemStack(Registry.ITEM.get(itemId)));
					} else {
						SkullMagic.LOGGER
								.warn("Failed trying to parse '" + itemIdentifyer + "' as specified in config.");
					}
				}
			}
		}

	}
}
