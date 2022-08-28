package com.romanvonklein.skullmagic.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

public class EssenceStatus {

    public static void drawEssenceStatus(MatrixStack matrixStack, float tickDelta) {

        // collect data to draw for player
        if (SkullMagic.clientEssenceManager != null) {
            int borderwidth = 5;
            int barwidth = 100;
            int barheight = 25;
            int pxPerEssence = 1;
            try {
                pxPerEssence = Math
                        .toIntExact(Math.round(100.0 / Double.valueOf(SkullMagic.clientEssenceManager.maxEssence)));

            } catch (Exception e) {
                SkullMagic.LOGGER.error(
                        "weird error calculating pxPerEssence with maxEssence: "
                                + SkullMagic.clientEssenceManager.maxEssence);
            }
            int x = 10;
            int y = 10;
            // border
            drawRect(matrixStack, x - borderwidth, y - borderwidth, barwidth + 2 * borderwidth,
                    barheight + 2 * borderwidth, 0xc2c2c2);
            // essence
            drawRect(matrixStack, x, y, SkullMagic.clientEssenceManager.essence * pxPerEssence, barheight, 0x114c9e);
            // empty
            drawRect(matrixStack, x + SkullMagic.clientEssenceManager.essence * pxPerEssence, y,
                    barwidth - SkullMagic.clientEssenceManager.essence * pxPerEssence, barheight, 0x787f8a);
        }

    }

    /**
     * Draws a rectangle on the screen
     * 
     * @param posX
     *               the x positon on the screen
     * @param posY
     *               the y positon on the screen
     * @param width
     *               the width of the rectangle
     * @param height
     *               the height of the rectangle
     * @param color
     *               the color of the rectangle
     */
    private static void drawRect(MatrixStack ms, int posX, int posY, int width, int height, int color) {
        if (color == -1)
            return;
        float f3;
        if (color <= 0xFFFFFF && color >= 0)
            f3 = 1.0F;
        else
            f3 = (color >> 24 & 255) / 255.0F;
        float f = (color >> 16 & 255) / 255.0F;
        float f1 = (color >> 8 & 255) / 255.0F;
        float f2 = (color & 255) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        BufferBuilder vertexbuffer = Tessellator.getInstance().getBuffer();
        vertexbuffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX, posY + height, 0).color(f, f1, f2, f3).next();
        vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX + width, posY + height, 0).color(f, f1, f2, f3).next();
        vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX + width, posY, 0).color(f, f1, f2, f3).next();
        vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX, posY, 0).color(f, f1, f2, f3).next();
        vertexbuffer.end();
        BufferRenderer.draw(vertexbuffer);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }
}
