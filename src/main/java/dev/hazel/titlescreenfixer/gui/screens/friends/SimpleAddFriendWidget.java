package dev.hazel.titlescreenfixer.gui.screens.friends;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.friends.AddFriendWidget;

public class SimpleAddFriendWidget extends AddFriendWidget {
    private final int clippedHeight;

    public SimpleAddFriendWidget(int width, Runnable afterSend) {
        super(width, afterSend);
        this.clippedHeight = 26;
        this.setHeight(this.clippedHeight);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.enableScissor(this.getX(), this.getY(),
                            this.getX() + this.getWidth(),
                            this.getY() + this.clippedHeight);
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        graphics.disableScissor();
    }

    @Override
    public int contentHeight() {
        return this.clippedHeight;
    }
}
