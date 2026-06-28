package dev.hazel.titlescreenfixer.mixin;

import dev.hazel.titlescreenfixer.FriendRequestHelper;
import dev.hazel.titlescreenfixer.FriendsOverlayScreenAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.screens.friends.FriendsOverlayScreen;
import net.minecraft.client.gui.screens.friends.FriendsScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OnlineOptionsScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.hazel.titlescreenfixer.TitleScreenFixer.ESSENTIAL_LOADED;
import static dev.hazel.titlescreenfixer.TitleScreenFixer.NOTIFICATION_DOT;

@Mixin(JoinMultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
    @Shadow
    private Button editButton;
    @Unique
    private Button friendsButton;
    @Unique
    private Button essentialFriendsButton;

    protected MultiplayerScreenMixin(Component title) {
        super(title);
    }

    @ModifyConstant(method = "<init>",
            constant = @Constant(intValue = 33))
    private int changeHeaderHeight(int original) {
        if (ESSENTIAL_LOADED) {
            return 33;
        }

        return 50;
    }

    @Redirect(method = "init",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addTitleHeader(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/Font;)V"))
    private void removeVanillaTitle(HeaderAndFooterLayout instance, Component component, Font font) {
        if (ESSENTIAL_LOADED) {
            instance.addTitleHeader(component, font);
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addButtons(CallbackInfo ci) {
        if (ESSENTIAL_LOADED) {
            essentialFriendsButton = this.addRenderableWidget(
                    CommonButtons.friends(
                            20,
                            _ -> OnlineOptionsScreen.confirmFriendsListEnabled(this.minecraft, () -> {
                                FriendsOverlayScreen overlay = new FriendsOverlayScreen(null);
                                ((FriendsOverlayScreenAccessor) overlay).titlescreenfixer$setLastScreen((Screen)(Object)this);
                                this.minecraft.gui.setScreen(overlay);
                            }, this),
                            !this.minecraft.isDemo()
                    )
            );
            essentialFriendsButton.setPosition(this.width - 26, this.height - 26);
            return;
        }

        int buttonWidth = 100;
        int spacing = 5;

        int totalWidth = buttonWidth * 2 + spacing;
        int startX = (this.width - totalWidth) / 2;
        int y = 6;

        Button serversButton = Button.builder(Component.literal("Servers"),
                btn -> {
                }).bounds(startX, y, buttonWidth, 20).build();

        serversButton.active = false;

        friendsButton = Button.builder(Component.literal("Friends"),
                btn -> OnlineOptionsScreen.confirmFriendsListEnabled(this.minecraft, () -> this.minecraft.setScreenAndShow(new FriendsScreen()), this)).bounds(startX + buttonWidth + spacing, y, buttonWidth, 20).build();

        this.addRenderableWidget(serversButton);
        this.addRenderableWidget(friendsButton);
    }

    @Override
    public void extractRenderState(final @NotNull GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        if (ESSENTIAL_LOADED) essentialFriendsButton.setPosition(this.width - 25, this.editButton.getY());

        if (FriendRequestHelper.hasPendingRequests(minecraft)) {
            int dotSize = 6;
            int dotX = friendsButton.getX() + friendsButton.getWidth() - dotSize + 2;
            int dotY = friendsButton.getY() - 2;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, NOTIFICATION_DOT, dotX, dotY, dotSize, dotSize);
        }
    }
}