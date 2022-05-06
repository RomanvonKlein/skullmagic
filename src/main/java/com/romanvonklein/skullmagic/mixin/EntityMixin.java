package com.romanvonklein.skullmagic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/*
@Mixin(TitleScreen.class)
public class SkullMagicMixin {
	@Inject(at = @At("HEAD"), method = "init()V")
	private void init(CallbackInfo info) {
		SkullMagic.LOGGER.info("This line is printed by an example mod mixin!");
	}
}
*/
@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	public abstract Vec3d getPos();
}