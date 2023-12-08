package com.romanvonklein.skullmagic.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BlockPlacerScreen extends HandledScreen<BlockPlacerScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(SkullMagic.MODID, "textures/gut/block_placer_gui.png");

    public BlockPlacerScreen(BlockPlacerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext drawContext, float var2, int var3, int var4) {
        // RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        // RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        // RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawContext.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    // @Override
    // public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
    // {
    // renderBackground(matrices);
    // super.render(matrices, mouseX, mouseY, delta);
    // drawMouseoverTooltip(matrices, mouseX, mouseY);
    // }

}
