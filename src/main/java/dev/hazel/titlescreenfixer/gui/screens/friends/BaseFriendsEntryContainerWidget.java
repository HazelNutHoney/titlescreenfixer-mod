package dev.hazel.titlescreenfixer.gui.screens.friends;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.friends.FriendsOverlayScreen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class BaseFriendsEntryContainerWidget extends AbstractContainerWidget {
    protected static final int SPRITE_TEXTURE_SIZE = 18;
    protected static final int BUTTON_SIZE = 20;
    protected static final int FACE_SIZE = 24;
    protected static final int PADDING = 4;
    protected static final int BACKGROUND_MARGIN = 4;

    protected final Minecraft minecraft;
    protected final FriendsOverlayScreen screen;
    protected final PlayerFaceWidget playerFaceWidget;
    protected final StringWidget nameWidget;
    protected final String playerName;
    protected final UUID playerId;
    protected final boolean showingStatus;
    private final List<AbstractWidget> children = new ArrayList<>();

    public BaseFriendsEntryContainerWidget(
            Minecraft minecraft,
            FriendsOverlayScreen screen,
            int x,
            int y,
            int width,
            int height,
            PlayerSocialManager.PlayerData playerData
    ) {
        this(minecraft, screen, x, y, width, height, playerData, false);
    }

    public BaseFriendsEntryContainerWidget(
            Minecraft minecraft,
            FriendsOverlayScreen screen,
            int x,
            int y,
            int width,
            int height,
            PlayerSocialManager.PlayerData playerData,
            boolean showingStatus
    ) {
        super(x, y, width, height, Component.empty());
        this.minecraft = minecraft;
        this.screen = screen;
        this.playerName = playerData.name();
        this.playerId = playerData.id();
        this.playerFaceWidget = new PlayerFaceWidget(FACE_SIZE, ResolvableProfile.createUnresolved(this.playerId));
        this.nameWidget = new StringWidget(Component.literal(this.playerName), minecraft.font);
        this.showingStatus = showingStatus;

        this.addChild(this.playerFaceWidget);
        this.addChild(this.nameWidget);
    }

    public abstract void disable();

    public UUID playerId() {
        return this.playerId;
    }

    protected abstract @NotNull Component getEntryNarration();

    public static Button.CreateNarration getSpriteIconNarration(Component actionDescription) {
        return narrationOutput -> Component.translatable("narrator.select", actionDescription);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.nameWidget.getMessage());
        GuiEventListener focused = this.getFocused();
        if (focused instanceof AbstractWidget focusedWidget) {
            focusedWidget.updateNarration(output.nest());
        } else {
            output.add(NarratedElementType.USAGE, this.getEntryNarration());
        }
    }

    @Override
    public @NotNull Collection<? extends NarratableEntry> getNarratables() {
        List<NarratableEntry> narratables = new ArrayList<>(this.children.size() + 1);
        narratables.addAll(this.children);
        narratables.add(this);
        return narratables;
    }

    protected final void addChild(AbstractWidget child) {
        this.children.add(child);
    }

    protected final void removeChild(AbstractWidget child) {
        if (this.children.remove(child) && this.getFocused() == child) {
            this.setFocused(null);
        }
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    protected int contentHeight() {
        return this.height;
    }

    protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft.options.highContrast().get()) {
            graphics.fill(
                    this.getX() - BACKGROUND_MARGIN,
                    this.getY(),
                    this.getX() + this.getWidth() + BACKGROUND_MARGIN,
                    this.getY() + this.getHeight(),
                    -16777216
            );
        }

        this.playerFaceWidget.setPosition(
                this.getX(),
                this.getY() + (this.getHeight() - this.playerFaceWidget.getHeight()) / 2
        );
        this.playerFaceWidget.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int nameY = this.getY() + this.getHeight() / (this.showingStatus ? 3 : 2) - this.nameWidget.getHeight() / 2;
        this.nameWidget.setPosition(this.playerFaceWidget.getRight() + PADDING, nameY);
        this.nameWidget.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
}
