package com.romanvonklein.skullmagic.mixin;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.romanvonklein.skullmagic.blockEntities.SkullMagicSkullBlockEntityRenderer;

import net.minecraft.block.SkullBlock;
import net.minecraft.block.SkullBlock.SkullType;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.resource.ResourceManager;

@Mixin(BuiltinModelItemRenderer.class)
public class HeadModelRegisteringMixin {
    // onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity
    // placer, ItemStack itemStack)
    @Shadow
    private Map<SkullBlock.SkullType, SkullBlockEntityModel> skullModels;
    @Shadow
    @Final
    private EntityModelLoader entityModelLoader;

    @Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("RETURN"))
    private void restrict(ResourceManager manager,
            CallbackInfo info) {
        HashMap<SkullType, SkullBlockEntityModel> map = new HashMap<>();

        map.putAll(SkullMagicSkullBlockEntityRenderer.getModels(this.entityModelLoader));
        map.putAll(SkullBlockEntityRenderer.getModels(this.entityModelLoader));
        this.skullModels = map;
    }

}
