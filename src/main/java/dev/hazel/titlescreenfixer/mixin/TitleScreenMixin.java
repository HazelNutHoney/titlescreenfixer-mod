package dev.hazel.titlescreenfixer.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FriendsButton;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void fixButtons(CallbackInfo ci) {
        FriendsButton friendsToRemove = null;

        for (GuiEventListener child : this.children()) {
            if (child instanceof FriendsButton friends) {
                friendsToRemove = friends;
            }

            if (child instanceof SpriteIconButton.CenteredIcon iconButton) {
                String text = iconButton.getMessage().getString();

                if (text.startsWith("Language")) {
                    iconButton.setX(this.width / 2 - 124);
                    iconButton.setY(this.height / 4 + 132);
                }

                if (text.startsWith("Accessibility")) {
                    iconButton.setX(this.width / 2 + 104);
                    iconButton.setY(this.height / 4 + 132);
                }
            }

            if (child instanceof Button button) {
                String text = button.getMessage().getString();

                if (text.equals("Options...")) {
                    button.setX(this.width / 2 - 100);
                    button.setY(this.height / 4 + 132);
                }

                if (text.equals("Quit Game")) {
                    button.setX(this.width / 2 + 2);
                    button.setY(this.height / 4 + 132);
                }
            }
        }

        if (friendsToRemove != null) {
            this.removeWidget(friendsToRemove);
        }
    }
}