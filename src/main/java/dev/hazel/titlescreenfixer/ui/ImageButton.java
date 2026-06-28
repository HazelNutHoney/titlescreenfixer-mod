package dev.hazel.titlescreenfixer.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ImageButton extends Button {
    private final Identifier icon;

    public ImageButton(Identifier icon, int x, int y, int w, int h, OnPress onPress) {
        this.icon = icon;

        super(x, y, w, h, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractDefaultSprite(graphics);

        graphics.blit(RenderPipelines.GUI_TEXTURED, this.icon, this.getX() + (this.width - 16) / 2, this.getY() + (this.height - 16) / 2, 0, 0, 16, 16, 16, 16);
    }
}
