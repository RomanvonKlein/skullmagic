package com.romanvonklein.skullmagic.blockEntities;

import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.collect.ImmutableMap;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blocks.SkullMagicSkullBlock.SkullMagicSkullType;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.SkullBlock.SkullType;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationPropertyHelper;

public class SkullMagicSkullBlockEntityRenderer extends SkullBlockEntityRenderer {
    private final Map<SkullBlock.SkullType, SkullBlockEntityModel> SKULLMAGIC_MODELS;
    private static final Map<SkullBlock.SkullType, Identifier> TEXTURES = Util.make(Maps.newHashMap(), map -> {
        map.put(SkullMagicSkullType.SPIDER, new Identifier("textures/entity/spider/spider.png"));
        map.put(SkullMagicSkullType.ENDERMAN, new Identifier("textures/entity/enderman/enderman.png"));
        map.put(SkullMagicSkullType.BLAZE, new Identifier("textures/entity/blaze/blaze.png"));
    });

    public SkullMagicSkullBlockEntityRenderer(Context ctx) {
        super(ctx);
        this.SKULLMAGIC_MODELS = SkullMagicSkullBlockEntityRenderer.getModels(ctx.getLayerRenderDispatcher());
    }

    public static Map<SkullType, SkullBlockEntityModel> getModels(EntityModelLoader modelLoader) {
        ImmutableMap.Builder<SkullType, SkullBlockEntityModel> builder = ImmutableMap
                .builder();
        TexturedModelData skullTexturedModelData = SkullEntityModel.getSkullTexturedModelData();
        TexturedModelData headTexturedModelData = SkullEntityModel.getHeadTexturedModelData();
        builder.put(SkullMagicSkullType.BLAZE,
                new SkullEntityModel(headTexturedModelData.createModel()));
        builder.put(SkullMagicSkullType.ENDERMAN,
                new SkullEntityModel(headTexturedModelData.createModel()));
        builder.put(SkullMagicSkullType.SPIDER,
                new SkullEntityModel(headTexturedModelData.createModel()));
        // builder.put(SkullMagicSkullType.BLAZE,
        // new
        // SkullEntityModel(modelLoader.getModelPart(SkullMagicEntityModelLayers.BLAZE_HEAD)));
        // builder.put(SkullMagicSkullType.ENDERMAN,
        // new
        // SkullEntityModel(modelLoader.getModelPart(SkullMagicEntityModelLayers.ENDERMAN_HEAD)));
        // builder.put(SkullMagicSkullType.SPIDER,
        // new
        // SkullEntityModel(modelLoader.getModelPart(SkullMagicEntityModelLayers.SPIDER_HEAD)));
        return builder.build();
    }

    public static RenderLayer getRenderLayer(SkullBlock.SkullType type, @Nullable GameProfile profile) {
        SkullMagic.LOGGER.info("GETTING RENDER LAYER_2!");
        Identifier identifier = TEXTURES.get(type);
        return RenderLayer.getEntityCutoutNoCullZOffset(identifier);
    }

    @Override
    public void render(SkullBlockEntity skullBlockEntity, float f, MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        float g = skullBlockEntity.getPoweredTicks(f);
        BlockState blockState = skullBlockEntity.getCachedState();
        boolean bl = blockState.getBlock() instanceof WallSkullBlock;
        Direction direction = bl ? blockState.get(WallSkullBlock.FACING) : null;
        int k = bl ? RotationPropertyHelper.fromDirection(direction.getOpposite())
                : blockState.get(SkullBlock.ROTATION);
        float h = RotationPropertyHelper.toDegrees(k);
        SkullBlock.SkullType skullType = ((AbstractSkullBlock) blockState.getBlock()).getSkullType();
        SkullBlockEntityModel skullBlockEntityModel = this.SKULLMAGIC_MODELS.get(skullType);
        SkullMagic.LOGGER.info("GETTING RENDER LAYER_1!");
        RenderLayer renderLayer = getRenderLayer(skullType, null);
        SkullBlockEntityRenderer.renderSkull(direction, h, g, matrixStack, vertexConsumerProvider, i,
                skullBlockEntityModel, renderLayer);
    }
/*
    public static void renderSkull(@Nullable Direction direction, float yaw, float animationProgress,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, SkullBlockEntityModel model,
            RenderLayer renderLayer) {
        matrices.push();
        if (direction == null) {
            matrices.translate(0.5f, 0.0f, 0.5f);
        } else {
            float f = 0.25f;
            matrices.translate(0.5f - (float) direction.getOffsetX() * 0.25f, 0.25f,
                    0.5f - (float) direction.getOffsetZ() * 0.25f);
        }
        matrices.scale(-1.0f, -1.0f, 1.0f);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);
        model.setHeadRotation(animationProgress, yaw, 0.0f);
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
        matrices.pop();
    }
*/
}
