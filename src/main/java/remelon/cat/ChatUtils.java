package remelon.cat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.List;

public final class ChatUtils {
    private static volatile boolean skipNextModify = false;
    private static volatile boolean transformNext = false;
    private ChatUtils() {
    }

    public static boolean shouldSkipNextModify() {
        return skipNextModify;
    }

    public static void setSkipNextModify(boolean v) {
        skipNextModify = v;
    }

    public static boolean shouldTransformNext() {
        return transformNext;
    }

    public static void setTransformNext(boolean v) {
        transformNext = v;
    }

    public static String getChatScreenText(ChatScreen chat) {
        if (chat == null) return null;
        try {
            List<?> children = chat.children();
            if (children != null) {
                for (Object child : children) {
                    if (child instanceof TextFieldWidget) {
                        return ((TextFieldWidget) child).getText();
                    }
                }
            }
        } catch (Throwable t) {
            Tsunderify.LOGGER.error("getChatScreenText failed: {}", t.getMessage());
            return null;
        }
        return null;
    }

    public static void setChatScreenText(ChatScreen chat, String text) {
        if (chat == null) return;
        try {
            List<?> children = chat.children();
            if (children != null) {
                for (Object child : children) {
                    if (child instanceof TextFieldWidget) {
                        ((TextFieldWidget) child).setText(text);
                        return;
                    }
                }
            }
        } catch (Throwable t) {
            Tsunderify.LOGGER.error("setChatScreenText failed: {}", t.getMessage());
        }
    }

    public static String[] transformCmd(String text) {
        if (text == null || text.isEmpty()) return null;

        String[] parts = extractCmdPrefix(text);
        if (parts == null) return null;

        String prefix = parts[0];
        String content = parts[1];

        String[] transformed = ChatTransformer.transformText(content);
        if (transformed == null) return null;

        return new String[]{prefix + transformed[0]};
    }

    private static String[] extractCmdPrefix(String text) {
        if (!text.startsWith("/")) return null;

        String[] noUser = new String[]{"/r ", "/reply ", "/me ", "/pc ", "/pchat ", "/party chat ", "/gc ", "/gchat ", "/guild chat ", "/oc ", "/ochat ", "/officer chat ", "/cc ", "/cchat ", "/coopchat ", "/ac "};
        for (String p : noUser) {
            if (text.startsWith(p)) {
                String content = text.substring(p.length());
                if (content.isEmpty()) return null;
                return new String[]{p, content};
            }
        }

        String[] withUser = new String[]{"/msg ", "/w ", "/message ", "/whisper "};
        for (String p : withUser) {
            if (text.startsWith(p)) {
                int start = p.length();
                int space = text.indexOf(' ', start);
                if (space == -1) return null;
                String username = text.substring(start, space);
                if (username.contains(" ")) return null;
                String content = text.substring(space + 1);
                if (content.isEmpty()) return null;
                return new String[]{p + username + " ", content};
            }
        }

        return null;
    }

    public static boolean handleTransform(MinecraftClient client, String text, boolean send) {
        if (text == null || text.isEmpty()) return false;

        String[] transformed;

        if (text.startsWith("/")) {
            transformed = transformCmd(text);
            if (transformed == null) {
                return false;
            }
        } else {
            transformed = ChatTransformer.transformText(text);
            if (transformed == null) return false;
        }

        if (send) {
            ClientPlayerEntity player = client.player;
            if (player != null) {
                setSkipNextModify(true);
                player.networkHandler.sendChatMessage(transformed[0]);
                setChatScreenText((ChatScreen) client.currentScreen, "");

                client.setScreen(null);
            }
        } else {
            ChatScreen screen = (ChatScreen) client.currentScreen;
            setChatScreenText(screen, transformed[0]);
            setSkipNextModify(true);
        }

        return true;
    }
}
