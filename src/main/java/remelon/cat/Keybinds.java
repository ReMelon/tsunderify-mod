package remelon.cat;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class Keybinds {
    private Keybinds() {}

    public static KeyBinding initKeyBinding() {
        //? if >= 1.21.9 {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("tsunderify", "category.tsunderify.chat"));
        KeyBinding kb = new KeyBinding("key.tsunderify.transform", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category);
        //?} else {
        // KeyBinding kb = new KeyBinding("key.tsunderify.transform", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.category.tsunderify.chat");
        //?}

        KeyBindingHelper.registerKeyBinding(kb);
        return kb;
    }
}
