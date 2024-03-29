package com.romanvonklein.skullmagic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;


@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	public abstract Vec3d getPos();
}