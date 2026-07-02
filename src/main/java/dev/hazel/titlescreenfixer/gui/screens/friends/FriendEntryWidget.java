package dev.hazel.titlescreenfixer.gui.screens.friends;

import com.mojang.authlib.yggdrasil.response.PresenceStatus;
import com.mojang.authlib.yggdrasil.response.PresenceStatusDto;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public class FriendEntryWidget extends BaseFriendsEntryContainerWidget {
    private static final WidgetSprites REMOVE_SPRITE = new WidgetSprites(Identifier.withDefaultNamespace("friends/remove"));
    private static final Component UNFRIEND = Component.translatable("gui.friends.unfriend");
    private static final Component CONFIRM_TITLE = Component.translatable("gui.friends.confirm_title");
    private static final Component CONFIRM_UNFRIEND = Component.translatable("gui.friends.confirm_unfriend");
    private static final Component PRESENCE_OFFLINE = Component.translatable("gui.friends.presence.status.offline").withColor(-6250336);

    private final StringWidget statusWidget;
    private final SpriteIconButton removeButton;
    private final Screen parentScreen;
    private final Runnable onUnfriend;
    private @Nullable PresenceStatusDto presence;

    public FriendEntryWidget(Minecraft minecraft, Screen parentScreen, PlayerSocialManager.PlayerData playerData, int width, @Nullable PresenceStatusDto presence, Runnable onUnfriend) {
        super(minecraft, null, 0, 0, width, 28, playerData, true);
        this.parentScreen = parentScreen;
        this.onUnfriend = onUnfriend;
        this.presence = presence;

        this.statusWidget = new StringWidget(presenceStatusComponent(presence), minecraft.font);
        this.addChild(this.statusWidget);

        this.removeButton = SpriteIconButton.builder(UNFRIEND, _ -> confirmRemove(), true)
                .size(20, 20)
                .sprite(REMOVE_SPRITE, 13, 11)
                .tooltip(UNFRIEND)
                .narration(getSpriteIconNarration(
                        Component.translatable("gui.friends.narration.button.unfriend", playerData.name())))
                .build();
        this.addChild(this.removeButton);
    }

    private static Component presenceStatusComponent(@Nullable PresenceStatusDto presence) {
        if (presence != null && presence.status() != PresenceStatus.OFFLINE) {
            String key = "gui.friends.presence.status." + presence.status().toString().toLowerCase(Locale.ROOT);
            return Component.translatable(key).withColor(-16711936);
        }

        return PRESENCE_OFFLINE;
    }

    public void applyPrensence(@Nullable PresenceStatusDto newPresence) {
        this.presence = newPresence;
        this.statusWidget.setMessage(presenceStatusComponent(newPresence));
    }

    private void confirmRemove() {
        System.out.println("Hello");
        this.minecraft.gui.setScreen(
                new PopupScreen.Builder(parentScreen, CONFIRM_TITLE)
                        .addMessage(CONFIRM_UNFRIEND)
                        .addButton(CommonComponents.GUI_REMOVE, _ -> {
                            this.removeButton.setLoading(true);
                            onUnfriend.run();
                            this.minecraft.gui.setScreen(parentScreen);
                        })
                        .addButton(CommonComponents.GUI_CANCEL, _ ->
                                this.minecraft.gui.setScreen(parentScreen))
                        .build()
        );
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!this.active || !this.visible) return false;

        if (removeButton.active
                && removeButton.isMouseOver(event.x(), event.y())) {
            System.out.println("Hello2");
            confirmRemove();
            return true;
        }
        return false;
    }

    public void disable() {
        this.removeButton.active = false;
    }

    @Override
    protected @NotNull Component getEntryNarration() {
        return Component.translatable("gui.friends.narration.entry.friend", this.playerName);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        int verticalCenter = this.getY() + (this.getHeight() - 20) / 2;
        int removeX = this.getX() + this.getWidth() - 20;
        this.removeButton.setPosition(removeX, verticalCenter);
        this.removeButton.extractRenderState(graphics, mouseX, mouseY, a);

        int statusWidgetX = this.playerFaceWidget.getRight() + 4;
        int statusWidth = removeX - statusWidgetX - 2;
        this.statusWidget.setMaxWidth(statusWidth, StringWidget.TextOverflow.SCROLLING);
        this.statusWidget.setPosition(statusWidgetX, this.nameWidget.getBottom() + 2);
        this.statusWidget.extractWidgetRenderState(graphics, mouseX, mouseY, a);
    }
}
