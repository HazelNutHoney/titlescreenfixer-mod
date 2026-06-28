package net.minecraft.client.gui.screens.friends;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class IncomingRequestEntryWidget extends AbstractFriendsEntryContainerWidget {
    private static final WidgetSprites ACCEPT_SPRITE = new WidgetSprites(Identifier.withDefaultNamespace("friends/accept"));
    private static final WidgetSprites DECLINE_SPRITE = new WidgetSprites(Identifier.withDefaultNamespace("friends/reject"));
    private static final Component ACCEPT = Component.translatable("gui.friends.accept");
    private static final Component DECLINE = Component.translatable("gui.friends.decline");

    private final SpriteIconButton acceptButton;
    private final SpriteIconButton declineButton;

    public IncomingRequestEntryWidget(Minecraft minecraft, PlayerSocialManager.PlayerData playerData, int width, Runnable onAccept, Runnable onDecline) {
        super(minecraft, null, 0, 0, width, 28, playerData, false);

        this.acceptButton = SpriteIconButton.builder(ACCEPT, _ -> onAccept.run(), true)
                .size(20, 20)
                .sprite(ACCEPT_SPRITE, 18, 18)
                .tooltip(ACCEPT)
                .narration(getSpriteIconNarration(
                        Component.translatable("gui.friends.narration.button.accept", playerData.name())))
                .build();

        this.declineButton = SpriteIconButton.builder(DECLINE, _ -> onDecline.run(), true)
                .size(20, 20)
                .sprite(DECLINE_SPRITE, 18, 18)
                .tooltip(DECLINE)
                .narration(getSpriteIconNarration(
                        Component.translatable("gui.friends.narration.button.decline", playerData.name())))
                .build();

        this.addChild(this.acceptButton);
        this.addChild(this.declineButton);
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
        if (!this.active || !this.visible) return false;
        if (acceptButton.active && acceptButton.isMouseOver(event.x(), event.y())) {
            acceptButton.onPress(event);
            return true;
        }
        if (declineButton.active && declineButton.isMouseOver(event.x(), event.y())) {
            declineButton.onPress(event);
            return true;
        }
        return false;
    }

    @Override
    void disable() {
        this.acceptButton.active = false;
        this.declineButton.active = false;
    }

    @Override
    protected @NotNull Component getEntryNarration() {
        return Component.translatable("gui.friends.narration.entry.incoming", this.playerName);
    }

    @Override
    protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        int verticalCenter = this.getY() + (this.getHeight() - 20) / 2;
        int declineX = this.getX() + this.getWidth() - 20;
        int acceptX = declineX - 22;
        this.acceptButton.setPosition(acceptX, verticalCenter);
        this.acceptButton.extractRenderState(graphics, mouseX, mouseY, a);
        this.declineButton.setPosition(declineX, verticalCenter);
        this.declineButton.extractRenderState(graphics, mouseX, mouseY, a);
    }
}
