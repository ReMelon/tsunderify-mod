package remelon.cat.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import remelon.cat.ChatUtils;
import remelon.cat.Tsunderify;
import remelon.cat.config.TsunderifyConfig;

/*? if >= 1.21.9 {*/
import net.minecraft.client.input.KeyEvent;
/*?}*/

@Mixin(EditBox.class)
public class TextFieldWidgetMixin {

    //? if >= 1.21.9 {
    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        int key = input.key();
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            Minecraft client = Minecraft.getInstance();
            if (client.screen instanceof ChatScreen) {
                String text = ChatUtils.getChatScreenText((ChatScreen) client.screen);
                if (text != null && TsunderifyConfig.CONFIG.instance().tsunifyOnEnter) {
                    if (ChatUtils.handleTransform(client, text, true)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
            return;
        }

        if (Tsunderify.keyBinding == null || !Tsunderify.keyBinding.matches(input)) return;

        Minecraft client = Minecraft.getInstance();
        if (!(client.screen instanceof ChatScreen)) return;

        String text = ChatUtils.getChatScreenText((ChatScreen) client.screen);
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
            Minecraft client = Minecraft.getInstance();
            if (client.screen instanceof ChatScreen) {
                String text = ChatUtils.getChatScreenText((ChatScreen) client.screen);
                if (text != null && TsunderifyConfig.CONFIG.instance().tsunifyOnEnter) {
                    if (ChatUtils.handleTransform(client, text, true)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
            return;
        }

        if (Tsunderify.keyBinding == null || !Tsunderify.keyBinding.matches(key, scanCode)) return;

        Minecraft client = Minecraft.getInstance();
        if (!(client.screen instanceof ChatScreen)) return;

        String text = ChatUtils.getChatScreenText((ChatScreen) client.screen);
        if (text == null || text.isEmpty()) return;

        boolean shouldSend = TsunderifyConfig.CONFIG.instance().specialKeySends;
        ChatUtils.handleTransform(client, text, shouldSend);
        cir.setReturnValue(true);
    }
    */
    //?} else if 1.21.4 {
    
    /*@Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int key, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            Minecraft client = Minecraft.getInstance();
            if (client.screen instanceof ChatScreen) {
                String text = ChatUtils.getChatScreenText((ChatScreen) client.screen);
                if (text != null && TsunderifyConfig.CONFIG.instance().tsunifyOnEnter) {
                    if (ChatUtils.handleTransform(client, text, true)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
            return;
        }

        if (Tsunderify.keyBinding == null || !Tsunderify.keyBinding.matches(key, scanCode)) return;

        Minecraft client = Minecraft.getInstance();
        if (!(client.screen instanceof ChatScreen)) return;

        String text = ChatUtils.getChatScreenText((ChatScreen) client.screen);
        if (text == null || text.isEmpty()) return;

        boolean shouldSend = TsunderifyConfig.CONFIG.instance().specialKeySends;
        ChatUtils.handleTransform(client, text, shouldSend);
        cir.setReturnValue(true);
    }
    
    */
    //?}
}
