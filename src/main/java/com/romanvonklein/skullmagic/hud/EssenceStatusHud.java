package com.romanvonklein.skullmagic.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.romanvonklein.skullmagic.ClientInitializer;
import com.romanvonklein.skullmagic.SkullMagic;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper.Argb;

public class EssenceStatusHud implements HudRenderCallback {
        private static final int essence_filled = Argb.getArgb(255, 17, 76, 158);
        private static final int essence_empty = Argb.getArgb(255, 120, 127, 138);
        private static final int spell_cooldown = Argb.getArgb(255, 255, 153, 51);
        private static final int spell_off_cooldown = Argb.getArgb(255, 102, 255, 102);
        private static final int spell_on_cooldown_text = Argb.getArgb(255, 204, 51, 0);
        private static final int spell_off_cooldown_text = Argb.getArgb(255, 51, 204, 51);
        private static final int essence_number_gray = Argb.getArgb(255, 194, 194, 194);

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
        private static void drawRect_2(MatrixStack ms, int posX, int posY, int width, int height, int color) {
                DrawableHelper.fill(ms, posX, posY, posX + width, posY + height,
                                color);
                // DrawableHelper.fill(ms, posX, posY,posX+ width,posY+ height, color);
        }

        /**
         * Draws a texture on the screen
         * 
         * @param posX
         *                the x positon on the screen
         * @param posY
         *                the y positon on the screen
         * @param width
         *                the width of the rectangle
         * @param height
         *                the height of the rectangle
         * @param texture
         *                the texture to draw
         */
        private static void drawTextureRect(MatrixStack ms, int posX, int posY, int width, int height,
                        Identifier texture) {
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                RenderSystem.setShaderTexture(0, texture);
                DrawableHelper.drawTexture(ms, posX, posY, 0, 0, width, height, width, height);

        }

        @Override
        public void onHudRender(MatrixStack matrixStack, float tickDelta) {
                // collect data to draw for player
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && ClientInitializer.getClientData() != null
                                && ClientInitializer.getClientData().isLinkedToAltar()) {
                        // TODO: cleanup - maybe make all sizes and positions cofigurable?
                        int symbolSpace = 16;
                        int borderwidth = 5;
                        int barwidth = 100;
                        int barheight = 5;
                        double pxPerEssence = 1.0;
                        int iconWidth = 16;
                        try {
                                pxPerEssence = ClientInitializer.getClientData().getMaxEssence() == 0 ? 1
                                                : Double.valueOf(barwidth)
                                                                / Double.valueOf(ClientInitializer
                                                                                .getClientData().getMaxEssence());

                        } catch (Exception e) {
                                SkullMagic.LOGGER.error(
                                                "weird error calculating pxPerEssence with maxEssence: "
                                                                + ClientInitializer
                                                                                .getClientData().getMaxEssence());
                        }
                        int x = 10 + symbolSpace + borderwidth;
                        int y = 10;
                        // essence
                        drawRect_2(matrixStack, x, y,
                                        Math.toIntExact(Math.round(Double
                                                        .valueOf(ClientInitializer.getClientData().getCurrentEssence())
                                                        * pxPerEssence)),
                                        barheight,
                                        essence_filled);
                        // empty
                        drawRect_2(matrixStack, x + Math.toIntExact(Math.round(Double
                                        .valueOf(ClientInitializer.getClientData().getCurrentEssence())
                                        * pxPerEssence)),
                                        y,
                                        barwidth - Math.toIntExact(Math.round(Double
                                                        .valueOf(ClientInitializer.getClientData().getCurrentEssence())
                                                        * pxPerEssence)),
                                        barheight, essence_empty);
                        // border
                        drawTextureRect(matrixStack, x - borderwidth, y - borderwidth, barwidth + 2 * borderwidth,
                                        barheight + 2 * borderwidth, ClientInitializer.ESSENCE_BAR_FRAME_TEXTURE);

                        // essence in numbers
                        client.textRenderer.draw(matrixStack,
                                        Double.valueOf(ClientInitializer.getClientData().getCurrentEssence())
                                                        + "/"
                                                        + Double.valueOf(ClientInitializer
                                                                        .getClientData().getMaxEssence()),
                                        x + 2 * borderwidth + barwidth,
                                        y - 1, essence_number_gray);

                        // spell cooldown bar
                        String spellname = ClientInitializer.getClientData().getSelectedSpellName();
                        if (spellname != null
                                        && ClientInitializer.getClientData().knowsSpell(spellname)) {

                                int cooldownLeft = ClientInitializer.getClientData().getCooldownLeftForSpell(spellname);
                                y += 3 * borderwidth + barheight;

                                int maxCoolDown = ClientInitializer.getClientData().getMaxCooldownForSpell(spellname);
                                int color = cooldownLeft > 0 ? spell_on_cooldown_text : spell_off_cooldown_text;
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
                                drawRect_2(matrixStack, x, y, cooldownBarWidth,
                                                barheight,
                                                spell_cooldown);
                                // empty
                                drawRect_2(matrixStack, x + cooldownBarWidth, y,
                                                rechargedBarWidth,
                                                barheight, spell_off_cooldown);
                                // border
                                drawTextureRect(matrixStack, x - borderwidth, y - borderwidth,
                                                barwidth + 2 * borderwidth,
                                                barheight + 2 * borderwidth,
                                                ClientInitializer.COOLDOWN_BAR_FRAME_TEXTURE);

                                // cooldown counter
                                if (cooldownLeft != 0) {
                                        client.textRenderer.draw(matrixStack,
                                                        Integer.toString(cooldownLeft / 20),
                                                        x + 2 * borderwidth + barwidth,
                                                        y - 1, color);
                                }

                                String selectedSpellName = ClientInitializer
                                                .getClientData().getSelectedSpellName();
                                String nextSpellName = ClientInitializer.getClientData()
                                                .getNextSpellname();
                                String prevSpellName = ClientInitializer.getClientData()
                                                .getPrevSpellname();

                                // icons
                                // previous
                                if (ClientInitializer.SPELL_ICONS.containsKey(prevSpellName)) {
                                        drawTextureRect(matrixStack,
                                                        x - iconWidth - borderwidth - iconWidth / 2,
                                                        y - borderwidth - iconWidth / 2,
                                                        iconWidth,
                                                        iconWidth,
                                                        ClientInitializer.SPELL_ICONS.get(prevSpellName));
                                }
                                // next
                                if (ClientInitializer.SPELL_ICONS.containsKey(nextSpellName)) {
                                        drawTextureRect(matrixStack,
                                                        x - iconWidth - borderwidth - iconWidth / 2,
                                                        y - borderwidth + iconWidth / 2,
                                                        iconWidth,
                                                        iconWidth,
                                                        ClientInitializer.SPELL_ICONS.get(nextSpellName));
                                }
                                // current
                                if (ClientInitializer.SPELL_ICONS.containsKey(selectedSpellName)) {
                                        drawTextureRect(matrixStack, x - iconWidth - borderwidth,
                                                        y - borderwidth,
                                                        iconWidth,
                                                        iconWidth,
                                                        ClientInitializer.SPELL_ICONS.get(selectedSpellName));
                                }

                        }
                }
        }
}
