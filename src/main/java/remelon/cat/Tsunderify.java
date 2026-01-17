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
    public static KeyBinding keyBinding;

    public static void handleTick(MinecraftClient client) {
        if (keyBinding == null) return;

        while (keyBinding.wasPressed()) {
            if (!(client.currentScreen instanceof ChatScreen)) return;

            String current = ChatUtils.getChatScreenText((ChatScreen) client.currentScreen);
            if (current == null || current.isEmpty()) return;

            if (!ChatUtils.handleTransform(client, current, TsunderifyConfig.CONFIG.instance().specialKeySends)) {
                return;
            }
        }
    }

    @Override
    public void onInitializeClient() {
        TsunderifyConfig.CONFIG.load();
        keyBinding = Keybinds.initKeyBinding();

        ClientSendMessageEvents.MODIFY_CHAT.register((raw) -> {
            if (ChatUtils.shouldSkipNextModify()) {
                ChatUtils.setSkipNextModify(false);
                return raw;
            }

            if (raw == null || raw.isEmpty()) return raw;

            if (raw.startsWith("/")) {
                try {
                    String[] cmdOut = ChatUtils.transformCmd(raw);
                    if (cmdOut != null) {
                        ChatUtils.setTransformNext(false);
                        return cmdOut[0];
                    }
                } catch (Throwable t) {
                    LOGGER.error("Error while transforming command '{}': {}", raw, t.getMessage());
                }
                ChatUtils.setTransformNext(false);
                return raw;
            }

            if (!ChatUtils.shouldTransformNext()) return raw;

            ChatUtils.setTransformNext(false);
            try {
                String[] out = ChatTransformer.transformText(raw);
                if (out != null) {
                    return out[0];
                }
            } catch (Throwable t) {
                LOGGER.error("Error while transforming chat '{}': {}", raw, t.getMessage());
            }

            return raw;
        });


        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                handleTick(client);
            } catch (Throwable t) {
                LOGGER.error("Error in Tsunderify tick handler", t);
            }
        });
    }
}