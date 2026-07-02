package dev.hazel.titlescreenfixer.gui.screens.friends;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class OutgoingRequestEntryWidget extends BaseFriendsEntryContainerWidget {
    private static final WidgetSprites REVOKE_SPRITE = new WidgetSprites(Identifier.withDefaultNamespace("friends/cancel"));
    private static final Component REVOKE = Component.translatable("gui.friends.cancel_request");

    private final SpriteIconButton revokeButton;

    public OutgoingRequestEntryWidget(Minecraft minecraft, PlayerSocialManager.PlayerData playerData, int width, Runnable onRevoke) {
        super(minecraft, null, 0, 0, width, 28, playerData, false);

        this.revokeButton = SpriteIconButton.builder(REVOKE, _ -> onRevoke.run(), true)
                .size(20, 20)
                .sprite(REVOKE_SPRITE, 12, 12)
                .tooltip(REVOKE)
                .narration(getSpriteIconNarration(
                        Component.translatable("gui.friends.narration.button.cancel_request", playerData.name())))
                .build();

        this.addChild(this.revokeButton);
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
        if (!this.active || !this.visible) return false;
        if (revokeButton.active && revokeButton.isMouseOver(event.x(), event.y())) {
            revokeButton.onPress(event);
            return true;
        }
        return false;
    }

    @Override
    public void disable() {
        this.revokeButton.active = false;
    }

    @Override
    protected @NotNull Component getEntryNarration() {
        return Component.translatable("gui.friends.narration.entry.outgoing", this.playerName);
    }

    @Override
    protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        int verticalCenter = this.getY() + (this.getHeight() - 20) / 2;
        int revokeX = this.getX() + this.getWidth() - 20;
        this.revokeButton.setPosition(revokeX, verticalCenter);
        this.revokeButton.extractRenderState(graphics, mouseX, mouseY, a);
    }
}