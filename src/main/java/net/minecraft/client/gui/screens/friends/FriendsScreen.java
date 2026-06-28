package net.minecraft.client.gui.screens.friends;

import com.mojang.authlib.yggdrasil.response.PresenceStatusDto;
import dev.hazel.titlescreenfixer.FriendRequestHelper;
import dev.hazel.titlescreenfixer.SimpleAddFriendWidget;
import dev.hazel.titlescreenfixer.TitleScreenFixer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.PrivacyConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonLinks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dev.hazel.titlescreenfixer.TitleScreenFixer.NOTIFICATION_DOT;

public class FriendsScreen extends Screen {
    private static final int CONTENT_WIDTH = 200;

    private LinearLayout contentLayout;
    private MultiLineTextWidget emptyText;
    private FrameLayout frame;
    private final List<FriendEntryWidget> friendWidgets = new ArrayList<>();
    private final List<Renderable> otherRenderables = new ArrayList<>();
    private final List<Renderable> emptyStateRenderables = new ArrayList<>();
    private int scissorX, scissorY, scissorX2, scissorY2;
    private Button requestsButton;
    private StringWidget profileNameLabel;
    private PlainTextButton profileNameButton;

    public FriendsScreen() {
        super(Component.nullToEmpty("Friends Screen"));
    }

    private <T extends GuiEventListener & Renderable & NarratableEntry> void addTrackedWidget(T widget) {
        otherRenderables.add(widget);
        addRenderableWidget(widget);
    }

    @Override
    protected void init() {
        super.init();
        otherRenderables.clear();

        int buttonWidth = 100;
        int spacing = 5;
        int totalWidth = buttonWidth * 2 + spacing;
        int startX = (this.width - totalWidth) / 2;
        int y = 6;

        Button serversButton = Button.builder(Component.literal("Servers"),
                        _ -> this.minecraft.setScreenAndShow(new JoinMultiplayerScreen(null)))
                .bounds(startX, y, buttonWidth, 20).build();

        Button friendsButton = Button.builder(Component.literal("Friends"), _ -> {
                })
                .bounds(startX + buttonWidth + spacing, y, buttonWidth, 20).build();
        friendsButton.active = false;

        this.addTrackedWidget(serversButton);
        this.addTrackedWidget(friendsButton);

        LinearLayout layout = LinearLayout.vertical();

        SimpleAddFriendWidget addFriendWidget = new SimpleAddFriendWidget(220, this::onRequestFinished);
        addFriendWidget.setX(startX + spacing);
        layout.addChild(addFriendWidget);

        contentLayout = new LinearLayout(CONTENT_WIDTH, 0, LinearLayout.Orientation.VERTICAL);
        contentLayout.spacing(4);

        int scrollHeight = this.height - addFriendWidget.contentHeight() - (y + 20);
        frame = new FrameLayout(CONTENT_WIDTH, scrollHeight);
        frame.addChild(contentLayout);
        frame.arrangeElements();

        ScrollableLayout scrollableLayout = new ScrollableLayout(
                this.minecraft, frame, scrollHeight, ScrollableLayout.ReserveStrategy.BOTH);
        layout.addChild(scrollableLayout);

        layout.arrangeElements();
        int layoutX = startX + (totalWidth - layout.getWidth()) / 2;
        layout.setPosition(layoutX, y + 20);
        layout.arrangeElements();
        layout.visitWidgets(w -> {
            otherRenderables.add(w);
            addRenderableWidget(w);
        });

        refreshFriendsList();

        requestsButton = SpriteIconButton.builder(
                        Component.literal("Requests"),
                        _ -> this.minecraft.gui.setScreen(new PendingRequestsScreen(this)),
                        true)
                .size(20, 20)
                .tooltip(Component.nullToEmpty("Requests"))
                .sprite(new WidgetSprites(Identifier.fromNamespaceAndPath(TitleScreenFixer.MOD_ID, "widget/icon_bell")), 14, 14)
                .build();
        requestsButton.setPosition(this.width - 26, this.height - 26);
        this.addTrackedWidget(requestsButton);

        scissorX  = scrollableLayout.getX();
        scissorY  = scrollableLayout.getY();
        scissorX2 = scrollableLayout.getX() + scrollableLayout.getWidth();
        scissorY2 = scrollableLayout.getY() + scrollableLayout.getHeight();

        scissorX  = scrollableLayout.getX();
        scissorY  = scrollableLayout.getY();
        scissorX2 = scrollableLayout.getX() + scrollableLayout.getWidth();
        scissorY2 = scrollableLayout.getY() + scrollableLayout.getHeight();

        String profileName = this.minecraft.getUser().getName();

        profileNameLabel = new StringWidget(
                Component.translatable("gui.friends.my_profile_name").withColor(0xAAAAAA),
                this.minecraft.font);

        profileNameButton = new PlainTextButton(
                0, 0,
                this.minecraft.font.width(profileName), 9,
                Component.literal(profileName).withColor(0xFFFFFF),
                _ -> this.minecraft.keyboardHandler.setClipboard(profileName),
                this.minecraft.font);
        profileNameButton.setTooltip(Tooltip.create(Component.translatable("gui.friends.copy_to_clipboard")));

        this.addTrackedWidget(profileNameLabel);
        this.addTrackedWidget(profileNameButton);

        repositionProfileName();
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
        for (var entry : friendWidgets) {
            if (entry.isMouseOver(event.x(), event.y())) {
                if (entry.mouseClicked(event, doubleClick)) {
                    return true;
                }
            }
        }

        boolean result = super.mouseClicked(event, doubleClick);

        if (emptyText != null
                && event.x() >= emptyText.getX()
                && event.x() <= emptyText.getX() + emptyText.getWidth()
                && event.y() >= emptyText.getY()
                && event.y() <= emptyText.getY() + emptyText.getHeight()) {

            int localY = (int) event.y() - emptyText.getY();
            int lineIndex = localY / 9;

            List<net.minecraft.util.FormattedCharSequence> lines =
                    this.minecraft.font.split(emptyText.getMessage(), emptyText.getWidth());

            if (lineIndex >= 0 && lineIndex < lines.size()) {
                net.minecraft.util.FormattedCharSequence line = lines.get(lineIndex);
                int lineWidth = this.minecraft.font.width(line);
                int lineStartX = emptyText.getX() + (emptyText.getWidth() - lineWidth) / 2;
                int localX = (int) event.x() - lineStartX;

                net.minecraft.network.chat.Style[] foundStyle = {null};
                float[] currentX = {0f};

                line.accept((_, style, codepoint) -> {
                    float advance = this.minecraft.font.getSplitter().stringWidth(
                            net.minecraft.network.chat.FormattedText.of(
                                    new String(Character.toChars(codepoint)), style));
                    if (currentX[0] <= localX && localX < currentX[0] + advance) {
                        foundStyle[0] = style;
                        return false;
                    }
                    currentX[0] += advance;
                    return true;
                });

                if (foundStyle[0] != null && foundStyle[0].getClickEvent() != null) {
                    try {
                        PrivacyConfirmLinkScreen.confirmLinkNow(this,
                                new java.net.URI(CommonLinks.PRIVACY_AND_ONLINE_SETTINGS.toString()));
                    } catch (Exception e) {
                        TitleScreenFixer.LOGGER.error(String.valueOf(e));
                    }
                    return true;
                }
            }
        }
        return result;
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();

        repositionProfileName();
    }

    @Override
    public void tick() {
        super.tick();

        this.minecraft.getPlayerSocialManager().getPresenceHandler().tryUpdatePresence();
    }

    private void onRequestFinished() {
        refreshFriendsList();
    }

    private void refreshFriendsList() {
        friendWidgets.forEach(this::removeWidget);
        friendWidgets.clear();
        emptyStateRenderables.forEach(r -> {
            if (r instanceof GuiEventListener l) removeWidget(l);
        });
        emptyStateRenderables.clear();
        contentLayout.removeChildren();

        var friends = this.minecraft.getPlayerSocialManager().getFriends();

        frame.removeChildren();

        if (friends.isEmpty()) {
            contentLayout.spacing(8);
            contentLayout.defaultCellSetting().alignHorizontallyCenter();
            ImageWidget illustration = ImageWidget.sprite(128, 48,
                    Identifier.withDefaultNamespace("friends/illustrations_00"));
            contentLayout.addChild(illustration);
            addRenderableWidget(illustration);
            emptyStateRenderables.add(illustration);

            Component microsoftLink = Component.translatable("gui.friends.empty_state.link")
                    .withStyle(style -> style
                            .withUnderlined(true)
                            .withColor(ChatFormatting.GRAY)
                            .withClickEvent(new ClickEvent.OpenUrl(CommonLinks.PRIVACY_AND_ONLINE_SETTINGS)));

            emptyText = new MultiLineTextWidget(
                    Component.translatable("gui.friends.empty_state", microsoftLink)
                            .withStyle(ChatFormatting.GRAY),
                    this.minecraft.font);
            emptyText.setMaxWidth(CONTENT_WIDTH);
            emptyText.setCentered(true);
            emptyText.setComponentClickHandler(_ -> {});
            contentLayout.addChild(emptyText);
            addRenderableWidget(emptyText);
            emptyStateRenderables.add(emptyText);

            frame.addChild(contentLayout, frame.newChildLayoutSettings()
                    .alignHorizontallyCenter().alignVerticallyMiddle());
        } else {
            emptyText = null;
            contentLayout.spacing(4);
            contentLayout.defaultCellSetting().alignHorizontallyLeft();

            var latestPresence = this.minecraft.getPlayerSocialManager().getPresenceHandler().getLatestPresence();

            for (var friend : friends) {
                PresenceStatusDto presence = null;
                for (PresenceStatusDto dto : latestPresence.presence()) {
                    if (dto.profileId().equals(friend.id())) {
                        presence = dto;
                        break;
                    }
                }

                var entry = new FriendEntryWidget(this.minecraft, this, friend, CONTENT_WIDTH + 2, presence, () -> unfriend(friend));
                contentLayout.addChild(entry);
//                addRenderableWidget(entry);
                addWidget(entry);
                friendWidgets.add(entry);
            }

            frame.addChild(contentLayout, frame.newChildLayoutSettings()
                    .alignHorizontallyCenter().alignVerticallyTop());
        }

        contentLayout.arrangeElements();
        frame.arrangeElements();
    }

    private void repositionProfileName() {
        if (profileNameLabel == null || profileNameButton == null) return;

        int labelWidth = this.minecraft.font.width(profileNameLabel.getMessage());
        int nameWidth = this.minecraft.font.width(profileNameButton.getMessage());
        int maxWidth = scissorX - 8 - 4;

        boolean wraps = (labelWidth + 4 + nameWidth) > maxWidth;

        if (wraps) {
            profileNameLabel.setPosition(8, this.height - 26);
            profileNameButton.setPosition(8, this.height - 14);
        } else {
            int rowY = this.height - 14;
            profileNameLabel.setPosition(8, rowY);
            profileNameButton.setPosition(8 + labelWidth, rowY);
        }
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        for (Renderable renderable : otherRenderables) {
            renderable.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }

        graphics.enableScissor(scissorX, scissorY, scissorX2, scissorY2);
        for (Renderable renderable : emptyStateRenderables) {
            renderable.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }

        for (FriendEntryWidget entry : friendWidgets) {
            entry.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
        graphics.disableScissor();

        if (FriendRequestHelper.hasPendingRequests(minecraft)) {
            int dotSize = 6;
            int dotX = requestsButton.getX() + requestsButton.getWidth() - dotSize + 2;
            int dotY = requestsButton.getY() - 2;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, NOTIFICATION_DOT, dotX, dotY, dotSize, dotSize);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(new JoinMultiplayerScreen(null));
    }

    private void unfriend(PlayerSocialManager.PlayerData friend) {
        this.minecraft.getPlayerSocialManager().removeFriend(friend.id())
                .thenRunAsync(this::refreshFriendsList, this.minecraft);
    }
}