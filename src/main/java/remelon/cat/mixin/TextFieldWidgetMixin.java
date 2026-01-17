package remelon.cat.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import remelon.cat.ChatUtils;
import remelon.cat.Tsunderify;
import remelon.cat.config.TsunderifyConfig;

/*? if >= 1.21.9 {*/
import net.minecraft.client.input.KeyInput;
/*?}*/

@Mixin(TextFieldWidget.class)
public class TextFieldWidgetMixin {

    //? if >= 1.21.9 {
    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyInput;)Z", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        int key = input.key();
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen instanceof ChatScreen) {
                String text = ChatUtils.getChatScreenText((ChatScreen) client.currentScreen);
                if (text != null && TsunderifyConfig.CONFIG.instance().tsunifyOnEnter) {
                    if (ChatUtils.handleTransform(client, text, true)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
            return;
        }

        if (Tsunderify.keyBinding == null || !Tsunderify.keyBinding.matchesKey(input)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof ChatScreen)) return;

        String text = ChatUtils.getChatScreenText((ChatScreen) client.currentScreen);
        if (text == null || text.isEmpty()) return;

        boolean shouldSend = TsunderifyConfig.CONFIG.instance().specialKeySends;
        ChatUtils.handleTransform(client, text, shouldSend);
        cir.setReturnValue(true);
    }
    //?} else if <= 1.21.8 && >= 1.21.5 {
    /*
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int key, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen instanceof ChatScreen) {
                String text = ChatUtils.getChatScreenText((ChatScreen) client.currentScreen);
                if (text != null && TsunderifyConfig.CONFIG.instance().tsunifyOnEnter) {
                    if (ChatUtils.handleTransform(client, text, true)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
            return;
        }

        if (Tsunderify.keyBinding == null || !Tsunderify.keyBinding.matchesKey(key, scanCode)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof ChatScreen)) return;

        String text = ChatUtils.getChatScreenText((ChatScreen) client.currentScreen);
        if (text == null || text.isEmpty()) return;

        boolean shouldSend = TsunderifyConfig.CONFIG.instance().specialKeySends;
        ChatUtils.handleTransform(client, text, shouldSend);
        cir.setReturnValue(true);
    }
    */
    //?} else if 1.21.4 {
    /*
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int key, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen instanceof ChatScreen) {
                String text = ChatUtils.getChatScreenText((ChatScreen) client.currentScreen);
                if (text != null && TsunderifyConfig.CONFIG.instance().tsunifyOnEnter) {
                    if (ChatUtils.handleTransform(client, text, true)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
            return;
        }

        if (Tsunderify.keyBinding == null || !Tsunderify.keyBinding.matchesKey(key, scanCode)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof ChatScreen)) return;

        String text = ChatUtils.getChatScreenText((ChatScreen) client.currentScreen);
        if (text == null || text.isEmpty()) return;

        boolean shouldSend = TsunderifyConfig.CONFIG.instance().specialKeySends;
        ChatUtils.handleTransform(client, text, shouldSend);
        cir.setReturnValue(true);
    }
    */
    //?}
}
