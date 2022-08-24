package com.romanvonklein.skullmagic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.Block;

@Mixin(Block.class)
public interface BlockInvoker {
    @Invoker("toString")
    public String toString();
}