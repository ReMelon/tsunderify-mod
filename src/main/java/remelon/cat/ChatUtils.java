package remelon.cat;

import net.minecraft.client.gui.screen.ChatScreen;

public final class ChatUtils {
    private ChatUtils() {
    }

    public static String getChatScreenText(ChatScreen chat) {
        try {
            try {
                java.lang.reflect.Method m = ChatScreen.class.getMethod("getChatField");
                Object tf = m.invoke(chat);
                java.lang.reflect.Method gt = tf.getClass().getMethod("getText");
                return (String) gt.invoke(tf);
            } catch (NoSuchMethodException ignored) {
            }

            try {
                java.lang.reflect.Method m2 = ChatScreen.class.getMethod("getText");
                return (String) m2.invoke(chat);
            } catch (NoSuchMethodException ignored) {
            }

            String[] fieldNames = new String[]{"inputField", "input", "chatField", "chatInput"};
            for (String fName : fieldNames) {
                try {
                    java.lang.reflect.Field f = ChatScreen.class.getDeclaredField(fName);
                    f.setAccessible(true);
                    Object tf = f.get(chat);
                    if (tf == null) continue;
                    java.lang.reflect.Method gt = tf.getClass().getMethod("getText");
                    return (String) gt.invoke(tf);
                } catch (NoSuchFieldException | NoSuchMethodException ignored) {
                }
            }
        } catch (Exception e) {
            Tsunderify.LOGGER.debug("Could not reflectively read ChatScreen input: {}", e.getMessage());
        }
        return null;
    }
}