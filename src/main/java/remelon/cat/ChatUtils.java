package remelon.cat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import remelon.cat.config.TsunderifyConfig;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class ChatUtils {
    private static volatile boolean skipNextModify = false;
    private static volatile boolean transformNext = false;
    private static volatile boolean tsunderifySource = false;

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

    public static boolean isTsunderifySource() {
        return tsunderifySource;
    }

    public static void setTsunderifySource(boolean v) {
        tsunderifySource = v;
    }

    public static String getChatScreenText(ChatScreen chat) {
        if (chat == null) return null;
        try {
            List<?> children = chat.children();
            if (children != null) {
                for (Object child : children) {
                    if (child instanceof EditBox) {
                        return ((EditBox) child).getValue();
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
                    if (child instanceof EditBox) {
                        ((EditBox) child).setValue(text);
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

        TsunderifyConfig config = TsunderifyConfig.CONFIG.instance();

        for (String raw : config.commands) {
            if (raw == null || raw.isEmpty()) continue;

            try {
                Pattern pattern = Pattern.compile(raw);
                Matcher matcher = pattern.matcher(text);
                if (!matcher.lookingAt()) continue;

                int prefixEnd = matcher.end();
                if (prefixEnd <= 0 || prefixEnd > text.length()) continue;

                String content = text.substring(prefixEnd);
                if (content.isEmpty()) return null;

                return new String[]{text.substring(0, prefixEnd), content};
            } catch (PatternSyntaxException e) {
                Tsunderify.LOGGER.error("Invalid command regex '{}': {}", raw, e.getMessage());
            }
        }

        return null;
    }

    public static boolean handleTransform(Minecraft client, String text, boolean send) {
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
            LocalPlayer player = client.player;
            if (player != null) {
                setSkipNextModify(true);
                setTsunderifySource(true);
                player.connection.sendChat(transformed[0]);
                setChatScreenText((ChatScreen) client.screen, "");
                client.setScreen(null);
            }
        } else {
            ChatScreen screen = (ChatScreen) client.screen;
            setChatScreenText(screen, transformed[0]);
            setSkipNextModify(true);
        }

        return true;
    }
}