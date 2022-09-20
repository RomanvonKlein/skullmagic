package com.romanvonklein.skullmagic.mixin;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.mob.ZombieVillagerEntity;

@Mixin(ZombieVillagerEntity.class)
public interface ZombieVillagerEntityMixin {
    @Invoker("setConverting")
    public void invokeSetConverting(@Nullable UUID uuid, int delay);
}
