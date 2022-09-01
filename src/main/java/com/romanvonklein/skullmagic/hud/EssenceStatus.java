package com.romanvonklein.skullmagic.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.romanvonklein.skullmagic.ClientInitializer;
import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.spells.SpellManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class EssenceStatus {

        public static void drawEssenceStatus(MatrixStack matrixStack, float tickDelta) {

                // collect data to draw for player
                if (ClientInitializer.getClientEssenceManager() != null) {
                        // TODO: cleanup - maybe make all sizes and positions cofigurable?
                        int symbolSpace = 16;
                        int borderwidth = 5;
                        int barwidth = 100;
                        int barheight = 5;
                        int pxPerEssence = 1;
                        int iconWidth = 16;
                        try {
                                pxPerEssence = ClientInitializer.getClientEssenceManager().maxEssence == 0 ? 1
                                                : Math.toIntExact(Math.round(Double.valueOf(barwidth)
                                                                / Double.valueOf(ClientInitializer
                                                                                .getClientEssenceManager().maxEssence)));

                        } catch (Exception e) {
                                SkullMagic.LOGGER.error(
                                                "weird error calculating pxPerEssence with maxEssence: "
                                                                + ClientInitializer
                                                                                .getClientEssenceManager().maxEssence);
                        }
                        int x = 10 + symbolSpace + borderwidth;
                        int y = 10;
                        // essence
                        drawRect(matrixStack, x, y, ClientInitializer.getClientEssenceManager().essence * pxPerEssence,
                                        barheight,
                                        0x114c9e);
                        // empty
                        drawRect(matrixStack, x + ClientInitializer.getClientEssenceManager().essence * pxPerEssence, y,
                                        barwidth - ClientInitializer.getClientEssenceManager().essence * pxPerEssence,
                                        barheight, 0x787f8a);
                        // border
                        drawTextureRect(matrixStack, x - borderwidth, y - borderwidth, barwidth + 2 * borderwidth,
                                        barheight + 2 * borderwidth, ClientInitializer.ESSENCE_BAR_FRAME_TEXTURE);

                        // essence in numbers

                        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
                        renderer.draw(matrixStack,
                                        Double.valueOf(ClientInitializer.getClientEssenceManager().essence) + "/"
                                                        + Double.valueOf(ClientInitializer
                                                                        .getClientEssenceManager().maxEssence),
                                        x + 2 * borderwidth + barwidth,
                                        y - 1, 0xc2c2c2);

                        // spell cooldown bar
                        String spellname = ClientInitializer.getClientSpellManager().selectedSpellName;
                        if (spellname != null && ClientInitializer.getClientSpellManager().spellList
                                        .containsKey(spellname)) {
                                int cooldownLeft = ClientInitializer.getClientSpellManager().spellList
                                                .get(spellname);
                                y += 3 * borderwidth + barheight;

                                int maxCoolDown = SpellManager.SpellDict
                                                .get(spellname).cooldownTicks;
                                int color = cooldownLeft > 0 ? 0xcc3300 : 0x33cc33;
                                double pxPerTick = 1;
                                try {
                                        pxPerTick = Double.valueOf(barwidth)
                                                        / Double.valueOf(maxCoolDown);
                                } catch (Exception e) {
                                        SkullMagic.LOGGER.error(
                                                        "could not calculate dimensions for hud rendering correctly");
                                }

                                // cooldown
                                int cooldownBarWidth = Math.toIntExact(Math.round(cooldownLeft * pxPerTick));
                                int rechargedBarWidth = barwidth - cooldownBarWidth;
                                drawRect(matrixStack, x, y, cooldownBarWidth,
                                                barheight,
                                                0xff9933);
                                // empty
                                drawRect(matrixStack, x + cooldownBarWidth, y,
                                                rechargedBarWidth,
                                                barheight, 0x66ff66);
                                // border
                                drawTextureRect(matrixStack, x - borderwidth, y - borderwidth,
                                                barwidth + 2 * borderwidth,
                                                barheight + 2 * borderwidth,
                                                ClientInitializer.COOLDOWN_BAR_FRAME_TEXTURE);

                                // cooldown counter
                                if (cooldownLeft != 0) {

                                        renderer.draw(matrixStack, Integer.toString(cooldownLeft / 20),
                                                        x + 2 * borderwidth + barwidth,
                                                        y - 1, color);
                                }

                                // icon
                                if (ClientInitializer.SPELL_ICONS.containsKey(ClientInitializer
                                                .getClientSpellManager().selectedSpellName)) {
                                        drawTextureRect(matrixStack, x - iconWidth - borderwidth, y - borderwidth,
                                                        iconWidth,
                                                        iconWidth, ClientInitializer.SPELL_ICONS.get(ClientInitializer
                                                                        .getClientSpellManager().selectedSpellName));
                                }

                        }
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
                vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX + width, posY + height, 0).color(f, f1, f2, f3)
                                .next();
                vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX + width, posY, 0).color(f, f1, f2, f3).next();
                vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX, posY, 0).color(f, f1, f2, f3).next();
                vertexbuffer.end();
                BufferRenderer.draw(vertexbuffer);
                RenderSystem.enableTexture();
                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();
        }

        /**
         * Draws a texture on the screen
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
        private static void drawTextureRect(MatrixStack ms, int posX, int posY, int width, int height,
                        Identifier texture) {
                RenderSystem.enableBlend();
                RenderSystem.enableTexture();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, texture);
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableDepthTest();
                BufferBuilder vertexbuffer = Tessellator.getInstance().getBuffer();
                vertexbuffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX, posY + height, 0).texture(0.0f, 1.0f).next();
                vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX + width, posY + height, 0).texture(1.0f, 1.0f)
                                .next();
                vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX + width, posY, 0).texture(1.0f, 0.0f).next();
                vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX, posY, 0).texture(0.0f, 0.0f).next();
                vertexbuffer.end();
                BufferRenderer.draw(vertexbuffer);
                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();
        }
}
