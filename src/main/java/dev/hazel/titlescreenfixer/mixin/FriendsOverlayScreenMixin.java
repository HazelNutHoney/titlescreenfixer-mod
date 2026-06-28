package dev.hazel.titlescreenfixer.mixin;

import dev.hazel.titlescreenfixer.FriendsOverlayScreenAccessor;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.friends.FriendsOverlayScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FriendsOverlayScreen.class)
public class FriendsOverlayScreenMixin extends Screen implements FriendsOverlayScreenAccessor {
    @Shadow
    @Nullable
    public LinearLayout layout;
    @Shadow
    @Nullable
    public TabNavigationBar tabNavigationBar;
    @Unique
    private Screen lastScreen;

    protected FriendsOverlayScreenMixin(Component title) {
        super(title);
    }

    @Override
    public void titlescreenfixer$setLastScreen(Screen screen) {
        this.lastScreen = screen;
    }

    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
    private void onClose(CallbackInfo ci) {
        if (lastScreen != null) {
            this.minecraft.gui.setScreen(lastScreen);
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (lastScreen == null) return;

        int panelLeft = ((FriendsOverlayScreen)(Object)this).layout.getX() - 8;
        int panelRight = this.layout.getX() + this.layout.getWidth() + 8;
        int panelTop = this.tabNavigationBar.getY();
        int panelBottom = this.layout.getY() + this.layout.getHeight() + 16;

        if (event.x() < panelLeft || event.x() > panelRight || event.y() < panelTop || event.y() > panelBottom) {
            this.minecraft.gui.setScreen(lastScreen);
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}