
package com.romanvonklein.skullmagic.blockEntities;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.romanvonklein.skullmagic.blocks.SkullMagicAbstractSkullBlock;
import com.romanvonklein.skullmagic.blocks.SkullMagicSkullBlock;
import com.romanvonklein.skullmagic.blocks.SkullMagicSkullBlock.SkullType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;

@Environment(value = EnvType.CLIENT)
public class SkullMagicSkullBlockEntityRenderer
        implements BlockEntityRenderer<SkullMagicSkullBlockEntity> {
    private final Map<SkullType, SkullBlockEntityModel> MODELS;
    private static final Map<SkullType, Identifier> TEXTURES = Util.make(Maps.newHashMap(),
            map -> {
                map.put(SkullType.ENDERMAN,
                        new Identifier("textures/entity/enderman/enderman.png"));
                map.put(SkullType.BLAZE,
                        new Identifier("textures/entity/blaze.png"));
                map.put(SkullType.SPIDER,
                        new Identifier("textures/entity/spider/spider.png"));
            });

    public static Map<SkullType, SkullBlockEntityModel> getModels(EntityModelLoader modelLoader) {
        ImmutableMap.Builder<SkullType, SkullBlockEntityModel> builder = ImmutableMap.builder();
        builder.put(SkullType.ENDERMAN,
                new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.SKELETON_SKULL)));
        builder.put(SkullType.BLAZE,
                new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.SKELETON_SKULL)));
        builder.put(SkullType.SPIDER,
                new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.SKELETON_SKULL)));
        return builder.build();
    }

    public SkullMagicSkullBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.MODELS = SkullMagicSkullBlockEntityRenderer.getModels(ctx.getLayerRenderDispatcher());
    }

    @Override
    public void render(SkullMagicSkullBlockEntity skullBlockEntity, float f, MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        float g = skullBlockEntity.getTicksPowered(f);
        BlockState blockState = skullBlockEntity.getCachedState();
        boolean bl = blockState.getBlock() instanceof WallSkullBlock;
        Direction direction = bl ? blockState.get(WallSkullBlock.FACING) : null;
        float h = 22.5f
                * (float) (bl ? (2 + direction.getHorizontal()) * 4 : blockState.get(SkullMagicSkullBlock.ROTATION));
        SkullType skullType = ((SkullMagicAbstractSkullBlock) blockState.getBlock())
                .getSkullType();
        SkullBlockEntityModel skullBlockEntityModel = this.MODELS.get(skullType);
        RenderLayer renderLayer = SkullMagicSkullBlockEntityRenderer.getRenderLayer(skullType);
        SkullMagicSkullBlockEntityRenderer.renderSkull(direction, h, g, matrixStack, vertexConsumerProvider, i,
                skullBlockEntityModel, renderLayer);
    }

    public static void renderSkull(@Nullable Direction direction, float yaw, float animationProgress,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, SkullBlockEntityModel model,
            RenderLayer renderLayer) {
        matrices.push();
        if (direction == null) {
            matrices.translate(0.5, 0.0, 0.5);
        } else {
            matrices.translate(0.5f - (float) direction.getOffsetX() * 0.25f, 0.25,
                    0.5f - (float) direction.getOffsetZ() * 0.25f);
        }
        matrices.scale(-1.0f, -1.0f, 1.0f);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);
        model.setHeadRotation(animationProgress, yaw, 0.0f);
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
        matrices.pop();
    }

    public static RenderLayer getRenderLayer(SkullType type) {
        Identifier identifier = TEXTURES.get(type);
        return RenderLayer.getEntityCutoutNoCullZOffset(identifier);
    }
}
