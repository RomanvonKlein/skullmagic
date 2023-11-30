package com.romanvonklein.skullmagic.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.romanvonklein.skullmagic.ClientInitializer;
import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.data.ClientData;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.text.Text;
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
        private static final int essence_pool_too_small_text = Argb.getArgb(255, 204, 51, 0);
        private static final int deduction_bar_can_afford = Argb.getArgb(255, 0, 255, 0);
        private static final int deduction_bar_cannot_afford = Argb.getArgb(255, 255, 0, 0);

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
        private static void drawRect(DrawContext drawContext, int posX, int posY, int width, int height, int color) {

                drawContext.fill(posX, posY, posX + width, posY + height,
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
        private static void drawTextureRect(DrawContext drawContext, int posX, int posY, int width, int height,
                        Identifier texture) {
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                // RenderSystem.setShaderTexture(0, texture);
                drawContext.drawTexture(texture, posX, posY, 0, 0, width, height, width, height);
        }

        @Override
        public void onHudRender(DrawContext drawContext, float tickDelta) {
                // collect data to draw for player
                MinecraftClient client = MinecraftClient.getInstance();
                ClientData clientData = ClientInitializer.getClientData();
                if (client != null && clientData != null
                                && clientData.isLinkedToAltar()) {
                        // TODO: cleanup - maybe make all sizes and positions cofigurable?
                        int symbolSpace = 16;
                        int borderwidth = 5;
                        int barwidth = 100;
                        int barheight = 5;
                        double pxPerEssence = 1.0;
                        int deduction_bar_height = 1;
                        int iconWidth = 16;
                        try {
                                pxPerEssence = clientData.getMaxEssence() == 0 ? 1
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
                        drawRect(drawContext, x, y,
                                        Math.toIntExact(Math.round(Double
                                                        .valueOf(clientData.getCurrentEssence())
                                                        * pxPerEssence)),
                                        barheight,
                                        essence_filled);
                        // empty
                        drawRect(drawContext, x + Math.toIntExact(Math.round(Double
                                        .valueOf(clientData.getCurrentEssence())
                                        * pxPerEssence)),
                                        y,
                                        barwidth - Math.toIntExact(Math.round(Double
                                                        .valueOf(clientData.getCurrentEssence())
                                                        * pxPerEssence)),
                                        barheight, essence_empty);
                        // spellcost deduction
                        if (clientData.getSelectedSpellData() != null) {

                                int fullEssenceCost = clientData.getSelectedSpellData().getEssenceCost();
                                if (fullEssenceCost <= clientData.getMaxEssence()) {
                                        int deductionBarColor = clientData.getCurrentEssence() >= fullEssenceCost
                                                        ? deduction_bar_can_afford
                                                        : deduction_bar_cannot_afford;
                                        drawRect(drawContext, x, y + barheight - deduction_bar_height, Math.toIntExact(
                                                        Math.round(Double.valueOf(fullEssenceCost) * pxPerEssence)),
                                                        deduction_bar_height, deductionBarColor);

                                }
                        }
                        // border
                        drawTextureRect(drawContext, x - borderwidth, y - borderwidth, barwidth + 2 * borderwidth,
                                        barheight + 2 * borderwidth, ClientInitializer.ESSENCE_BAR_FRAME_TEXTURE);

                        // essence in numbers
                        drawContext.drawText(client.textRenderer,
                                        Double.valueOf(clientData.getCurrentEssence())
                                                        + "/"
                                                        + Double.valueOf(ClientInitializer
                                                                        .getClientData().getMaxEssence()),
                                        x + 2 * borderwidth + barwidth,
                                        y - 1, essence_number_gray, false);

                        // spell cooldown bar
                        String spellname = clientData.getSelectedSpellName();
                        if (spellname != null
                                        && clientData.knowsSpell(spellname)) {
                                int fullEssenceCost = clientData.getSelectedSpellData().getEssenceCost();
                                int cooldownLeft = clientData.getCooldownLeftForSpell(spellname);
                                y += 3 * borderwidth + barheight;

                                int maxCoolDown = clientData.getMaxCooldownForSpell(spellname);
                                int color = cooldownLeft > 0 ? spell_on_cooldown_text : spell_off_cooldown_text;
                                double pxPerTick = 1;
                                try {
                                        pxPerTick = Double.valueOf(barwidth)
                                                        / Double.valueOf(maxCoolDown);
                                } catch (Exception e) {
                                        SkullMagic.LOGGER.error(
                                                        "could not calculate dimensions for hud rendering correctly");
                                }
                                // Draw cooldown bar if player's essence Pool is large enough

                                if (fullEssenceCost > clientData.getMaxEssence()) {
                                        drawContext.drawText(client.textRenderer,
                                                        Text.translatable("skullmagic.gui.essence_pool_too_small",
                                                                        fullEssenceCost),
                                                        x,
                                                        y, essence_pool_too_small_text, false);
                                } else {
                                        // cooldown
                                        int cooldownBarWidth = Math.toIntExact(Math.round(cooldownLeft * pxPerTick));
                                        int rechargedBarWidth = barwidth - cooldownBarWidth;
                                        drawRect(drawContext, x, y, cooldownBarWidth,
                                                        barheight,
                                                        spell_cooldown);
                                        // empty
                                        drawRect(drawContext, x + cooldownBarWidth, y,
                                                        rechargedBarWidth,
                                                        barheight, spell_off_cooldown);
                                        // border
                                        drawTextureRect(drawContext, x - borderwidth, y - borderwidth,
                                                        barwidth + 2 * borderwidth,
                                                        barheight + 2 * borderwidth,
                                                        ClientInitializer.COOLDOWN_BAR_FRAME_TEXTURE);

                                        // autocast indicator
                                        if (clientData.shouldAutocastSpell(spellname)) {
                                                drawTextureRect(drawContext, x + borderwidth + barwidth,
                                                                y - borderwidth,
                                                                iconWidth,
                                                                iconWidth,
                                                                ClientInitializer.AUTOCAST_INDICATOR);
                                        }
                                        // cooldown counter
                                        if (cooldownLeft != 0) {
                                                drawContext.drawText(client.textRenderer,
                                                                Integer.toString(cooldownLeft / 20),
                                                                x + 2 * borderwidth + barwidth,
                                                                y - 1, color, false);
                                        }
                                }

                                String selectedSpellName = ClientInitializer
                                                .getClientData().getSelectedSpellName();
                                String nextSpellName = clientData
                                                .getNextSpellname();
                                String prevSpellName = clientData
                                                .getPrevSpellname();

                                // icons
                                // previous
                                if (ClientInitializer.SPELL_ICONS.containsKey(prevSpellName)) {
                                        drawTextureRect(drawContext,
                                                        x - iconWidth - borderwidth - iconWidth / 2,
                                                        y - borderwidth - iconWidth / 2,
                                                        iconWidth,
                                                        iconWidth,
                                                        ClientInitializer.SPELL_ICONS.get(prevSpellName));
                                }
                                // next
                                if (ClientInitializer.SPELL_ICONS.containsKey(nextSpellName)) {
                                        drawTextureRect(drawContext,
                                                        x - iconWidth - borderwidth - iconWidth / 2,
                                                        y - borderwidth + iconWidth / 2,
                                                        iconWidth,
                                                        iconWidth,
                                                        ClientInitializer.SPELL_ICONS.get(nextSpellName));
                                }
                                // current
                                if (ClientInitializer.SPELL_ICONS.containsKey(selectedSpellName)) {
                                        drawTextureRect(drawContext, x - iconWidth - borderwidth,
                                                        y - borderwidth,
                                                        iconWidth,
                                                        iconWidth,
                                                        ClientInitializer.SPELL_ICONS.get(selectedSpellName));
                                }

                        }
                }
        }

}
