package com.romanvonklein.skullmagic.blockEntities;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Quaternion;

@Environment(EnvType.CLIENT)
public class ItemHolderBlockEntityRendererShrine implements BlockEntityRenderer<SpellShrineBlockEntity> {
    public ItemHolderBlockEntityRendererShrine(BlockEntityRendererFactory.Context context) {

    }

    public void render(SpellShrineBlockEntity blockEntity, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, int overlay) {

        if (blockEntity.getScroll() != null) {
            matrices.push();

            // Calculate the current offset in the y value
            double offset = Math.sin((blockEntity.getWorld().getTime() + tickDelta) / 8.0) / 8.0;
            // Move the item
            matrices.translate(0.5, 1.25 + offset, 0.5);

            // Rotate the item
            Quaternion q = new Quaternion(0.0f, 1.0f, 0.0f, (blockEntity.getWorld().getTime() + tickDelta) * 4);

            matrices.multiply(q);

            int lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up());

            MinecraftClient.getInstance().getItemRenderer().renderItem(blockEntity.getScroll(),
                    ModelTransformation.Mode.GROUND,
                    lightAbove,
                    OverlayTexture.DEFAULT_UV,
                    matrices,
                    vertexConsumers,
                    0);

            // Mandatory call after GL calls
            matrices.pop();
        }
    }
}
