package dev.hazel.titlescreenfixer.gui.screens.friends;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.friends.AbstractFriendsEntryContainerWidget;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PendingRequestsScreen extends Screen {
    private static final int CONTENT_WIDTH = 200;

    private final Screen parent;
    private LinearLayout contentLayout;
    private FrameLayout frame;
    private final List<Renderable> requestRenderables = new ArrayList<>();
    private int scissorX, scissorY, scissorX2, scissorY2;

    protected PendingRequestsScreen(Screen parent) {
        super(Component.nullToEmpty("Requests Screen"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 100;
        int spacing = 5;
        int totalWidth = buttonWidth * 2 + spacing;
        int startX = (this.width - totalWidth) / 2;
        int y = 6;

        Button serversButton = Button.builder(Component.literal("Servers"),
                        btn -> this.minecraft.setScreenAndShow(new JoinMultiplayerScreen(null)))
                .bounds(startX, y, buttonWidth, 20).build();

        Button friendsButton = Button.builder(Component.literal("Friends"), btn -> {
                })
                .bounds(startX + buttonWidth + spacing, y, buttonWidth, 20).build();
        friendsButton.active = false;

        this.addRenderableWidget(serversButton);
        this.addRenderableWidget(friendsButton);

        Button closeButton = SpriteIconButton.builder(
                        Component.literal("Close"),
                        _ -> this.minecraft.gui.setScreen(parent),
                        true)
                .size(20, 20)
                .tooltip(Component.nullToEmpty("Close"))
                .sprite(new WidgetSprites(Identifier.withDefaultNamespace("friends/reject")), 18, 18)
                .build();
        closeButton.setPosition(this.width - 26, this.height - 26);
        this.addRenderableWidget(closeButton);

        contentLayout = new LinearLayout(CONTENT_WIDTH, 0, LinearLayout.Orientation.VERTICAL);
        contentLayout.spacing(4);

        int scrollHeight = this.height - (y + 20) - 30;
        frame = new FrameLayout(CONTENT_WIDTH, scrollHeight);
        frame.addChild(contentLayout);
        frame.arrangeElements();

        ScrollableLayout scrollableLayout = new ScrollableLayout(
                this.minecraft, frame, scrollHeight, ScrollableLayout.ReserveStrategy.BOTH);

        LinearLayout layout = LinearLayout.vertical();
        layout.addChild(scrollableLayout);
        layout.arrangeElements();
        layout.setPosition((this.width - layout.getWidth()) / 2, y + 26);
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);

        refreshRequests();

        scissorX  = scrollableLayout.getX();
        scissorY  = scrollableLayout.getY();
        scissorX2 = scrollableLayout.getX() + scrollableLayout.getWidth();
        scissorY2 = scrollableLayout.getY() + scrollableLayout.getHeight();
    }

    private void refreshRequests() {
        requestRenderables.forEach(r -> {
            if (r instanceof GuiEventListener l) removeWidget(l);
        });
        requestRenderables.clear();
        contentLayout.removeChildren();
        frame.removeChildren();

        var incoming = this.minecraft.getPlayerSocialManager().getIncomingRequests();
        var outgoing = this.minecraft.getPlayerSocialManager().getOutgoingRequests();

        contentLayout.defaultCellSetting().alignHorizontallyLeft();
        contentLayout.spacing(4);

        for (var request : incoming) {
            var entry = new IncomingRequestEntryWidget(this.minecraft, request, CONTENT_WIDTH,
                    () -> {
                        this.minecraft.getPlayerSocialManager().acceptIncomingFriendRequest(request.id())
                                .thenRunAsync(this::refreshRequests, this.minecraft);
                    },
                    () -> {
                        this.minecraft.getPlayerSocialManager().declineIncomingFriendRequest(request.id())
                                .thenRunAsync(this::refreshRequests, this.minecraft);
                    });
            contentLayout.addChild(entry);
            addWidget(entry);
            requestRenderables.add(entry);
        }

        for (var request : outgoing) {
            var entry = new OutgoingRequestEntryWidget(this.minecraft, request, CONTENT_WIDTH,
                    () -> {
                        this.minecraft.getPlayerSocialManager().revokeOutgoingFriendRequest(request.id())
                                .thenRunAsync(this::refreshRequests, this.minecraft);
                    });
            contentLayout.addChild(entry);
            addWidget(entry);
            requestRenderables.add(entry);
        }

        frame.addChild(contentLayout, frame.newChildLayoutSettings()
                .alignHorizontallyCenter().alignVerticallyTop());
        contentLayout.arrangeElements();
        frame.arrangeElements();
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.enableScissor(scissorX, scissorY, scissorX2, scissorY2);
        for (Renderable r : requestRenderables) {
            r.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
        graphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
        for (Renderable r : requestRenderables) {
            if (r instanceof AbstractFriendsEntryContainerWidget entry
                    && entry.isMouseOver(event.x(), event.y())) {
                if (entry.mouseClicked(event, doubleClick)) return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }
}
