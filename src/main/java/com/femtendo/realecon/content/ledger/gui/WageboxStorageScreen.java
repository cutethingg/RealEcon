package com.femtendo.realecon.content.ledger.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WageboxStorageScreen extends AbstractContainerScreen<com.femtendo.realecon.content.wagebox.WageboxStorageMenu> {
    // Standard 54-slot chest texture, but we will only draw the top half
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    public WageboxStorageScreen(com.femtendo.realecon.content.wagebox.WageboxStorageMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 167; // Trimmed height for 3 rows
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Draw top part of chest texture
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, 71);
        // Draw bottom part (Player inventory)
        graphics.blit(TEXTURE, x, y + 71, 0, 126, imageWidth, 96);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}