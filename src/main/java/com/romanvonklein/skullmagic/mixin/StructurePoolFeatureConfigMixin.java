package com.romanvonklein.skullmagic.mixin;

import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

@Mixin(StructurePoolFeatureConfig.class)
public class StructurePoolFeatureConfigMixin {

    @Inject(method = "getSize(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V", at = @At("TAIL"))
    public void getSizeMixin(CallbackInfoReturnable ci) {
    }
}
