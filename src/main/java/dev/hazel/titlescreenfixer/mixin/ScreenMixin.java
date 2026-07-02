package dev.hazel.titlescreenfixer.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.hazel.titlescreenfixer.TitlescreenFixer.ESSENTIAL_LOADED;

@Mixin(Screen.class)
public class ScreenMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;

    @Shadow
    public int width;

    @Shadow
    @Final
    protected Font font;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (!(this.minecraft.gui.screen() instanceof JoinMultiplayerScreen) || ESSENTIAL_LOADED) return;

        MutableComponent text = Component.translatable("multiplayer.title");
        int textY = 6 + 20 + 3;
        int textWidth = this.font.width(text);

        int x = (this.width - textWidth) / 2;

        graphics.text(this.font, text,
                x, textY + 5,
                0xFFFFFFFF, false);
    }
}
