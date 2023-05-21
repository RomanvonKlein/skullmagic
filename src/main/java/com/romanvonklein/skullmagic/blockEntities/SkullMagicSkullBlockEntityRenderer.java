package com.romanvonklein.skullmagic.blockEntities;

import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.collect.ImmutableMap;

import com.google.common.collect.Maps;
import com.romanvonklein.skullmagic.blocks.AbstractSkullMagicSkullBlock;
import com.romanvonklein.skullmagic.blocks.SkullMagicSkullBlock;
import com.romanvonklein.skullmagic.blocks.SkullMagicSkullBlock.SkullMagicSkullType;
import com.romanvonklein.skullmagic.blocks.SkullMagicSkullBlock.SkullMagicType;

import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationPropertyHelper;

public class SkullMagicSkullBlockEntityRenderer
                implements BlockEntityRenderer<SkullMagicSkullBlockEntity> {
        private final Map<SkullMagicType, SkullBlockEntityModel> SKULLMAGIC_MODELS;
        private static final Map<SkullMagicType, Identifier> TEXTURES = Util.make(Maps.newHashMap(), map -> {
                map.put(SkullMagicSkullBlock.SkullMagicType.SPIDER,
                                new Identifier("textures/entity/spider/spider.png"));
                map.put(SkullMagicSkullBlock.SkullMagicType.ENDERMAN,
                                new Identifier("textures/entity/enderman/enderman.png"));
                map.put(SkullMagicSkullBlock.SkullMagicType.BLAZE, new Identifier("textures/entity/blaze.png"));
        });

        public SkullMagicSkullBlockEntityRenderer(Context ctx) {
                this.SKULLMAGIC_MODELS = SkullMagicSkullBlockEntityRenderer.getModels(ctx.getLayerRenderDispatcher());
        }

        public static ModelData getModelData() {
                ModelData modelData = new ModelData();
                ModelPartData modelPartData = modelData.getRoot();
                modelPartData.addChild(EntityModelPartNames.HEAD,
                                ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f),
                                ModelTransform.NONE);
                return modelData;
        }

        public static TexturedModelData getHeadTexturedModelData() {
                ModelData modelData = getModelData();
                ModelPartData modelPartData = modelData.getRoot();
                modelPartData.getChild(EntityModelPartNames.HEAD)
                                .addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f,
                                                -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new Dilation(0.25f)),
                                                ModelTransform.NONE);
                return TexturedModelData.of(modelData, 64, 32);
        }

        public static Map<SkullMagicType, SkullBlockEntityModel> getModels(EntityModelLoader modelLoader) {
                ImmutableMap.Builder<SkullMagicType, SkullBlockEntityModel> builder = ImmutableMap
                                .builder();
                // TexturedModelData skullTexturedModelData =
                // SkullEntityModel.getSkullTexturedModelData();
                TexturedModelData headTexturedModelData = getHeadTexturedModelData();
                builder.put(SkullMagicSkullBlock.SkullMagicType.BLAZE,
                                new SkullEntityModel(headTexturedModelData.createModel()));
                builder.put(SkullMagicSkullBlock.SkullMagicType.ENDERMAN,
                                new SkullEntityModel(headTexturedModelData.createModel()));
                builder.put(SkullMagicSkullBlock.SkullMagicType.SPIDER,
                                new SkullEntityModel(headTexturedModelData.createModel()));
                return builder.build();
        }

        public static RenderLayer getRenderLayer(SkullMagicSkullType type) {
                Identifier identifier = TEXTURES.get(type);
                return RenderLayer.getEntityCutoutNoCullZOffset(identifier);
        }

        @Override
        public void render(SkullMagicSkullBlockEntity skullBlockEntity, float f, MatrixStack matrixStack,
                        VertexConsumerProvider vertexConsumerProvider, int i, int j) {

                BlockState blockState = skullBlockEntity.getCachedState();
                boolean isWallSkullBlock = blockState.getBlock() instanceof WallSkullBlock;
                Direction direction = isWallSkullBlock ? blockState.get(WallSkullBlock.FACING) : null;
                int k = isWallSkullBlock && direction != null
                                ? RotationPropertyHelper.fromDirection(direction.getOpposite())
                                : blockState.get(SkullBlock.ROTATION);
                float h = RotationPropertyHelper.toDegrees(k);
                SkullMagicSkullBlock.SkullMagicSkullType skullType = ((AbstractSkullMagicSkullBlock) blockState
                                .getBlock())
                                .getSkullType();
                SkullBlockEntityModel skullBlockEntityModel = this.SKULLMAGIC_MODELS.get(skullType);
                RenderLayer renderLayer = SkullMagicSkullBlockEntityRenderer.getRenderLayer(skullType);
                SkullMagicSkullBlockEntityRenderer.renderSkull(direction, h, 0.0f, matrixStack, vertexConsumerProvider,
                                i,
                                skullBlockEntityModel, renderLayer);
        }

        public static void renderSkull(@Nullable Direction direction,
                        float yaw, float animationProgress,
                        MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                        SkullBlockEntityModel model,
                        RenderLayer renderLayer) {
                matrices.push();
                if (direction == null) {
                        matrices.translate(0.5f, 0.0f, 0.5f);
                } else {
                        matrices.translate(0.5f - direction.getOffsetX() * 0.25f, 0.25f,
                                        0.5f - direction.getOffsetZ() * 0.25f);
                }
                matrices.scale(-1.0f, -1.0f, 1.0f);
                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);
                model.setHeadRotation(animationProgress, yaw, 0.0f);
                model.render(matrices, vertexConsumer, light, OverlayTexture.packUv(0, 0),
                                1.0f, 1.0f, 1.0f, 1.0f);
                matrices.pop();
        }

}
