package remelon.cat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import remelon.cat.config.TsunderifyConfig;

public class Tsunderify implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("tsunderify");
    public static final String VERSION = "0.1.0";
    public static final String MINECRAFT = "1.21.10";
    public static KeyBinding keyBinding;
    public static volatile boolean nextSendShouldTransform = false;

    public static void handleTick(MinecraftClient client) {
        if (Tsunderify.keyBinding == null) return;
        while (Tsunderify.keyBinding.wasPressed()) {
            net.minecraft.client.gui.screen.Screen scr = client.currentScreen;
            if (scr instanceof ChatScreen chat) {

                String current = ChatUtils.getChatScreenText(chat);
                if (current == null) current = "";

                if (current.startsWith("/")) {
                    return;
                }

                String[] out = ChatTransformer.transformText(current);
                if (out == null) {
                    return;
                }
                String transformed = out[0];

                if (TsunderifyConfig.CONFIG.instance().specialKeySends) {
                    if (client.player != null) {
                        assert MinecraftClient.getInstance().player != null;
                        MinecraftClient.getInstance().player.networkHandler.sendChatMessage(transformed);
                        client.setScreen(null);
                    }
                }
            }
        }
    }

    @Override
    public void onInitializeClient() {
        TsunderifyConfig.CONFIG.load();
        keyBinding = Keybinds.initKeyBinding();

        ClientSendMessageEvents.MODIFY_CHAT.register((raw) -> {
            if (raw == null) return raw;
            if (raw.startsWith("/")) {
                Tsunderify.nextSendShouldTransform = false;
                return raw;
            }
            if (!Tsunderify.nextSendShouldTransform) return raw;
            Tsunderify.nextSendShouldTransform = false;
            String[] out = ChatTransformer.transformText(raw);
            if (out != null) {
                Tsunderify.LOGGER.debug("Transformed outgoing chat: '{}' -> '{}'", raw, out[0]);
                return out[0];
            }
            return raw;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                handleTick(client);
            } catch (Throwable t) {
                Tsunderify.LOGGER.error("Error in Tsunderify tick handler", t);
            }
        });
    }
}