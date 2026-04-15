package remelon.cat;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class Keybinds {
    private Keybinds() {}

    public static KeyMapping initKeyBinding() {
        //? if >= 1.21.9 {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("tsunderify", "category.tsunderify.chat"));
        KeyMapping kb = new KeyMapping("key.tsunderify.transform", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category);
        //?} else {
        /*KeyMapping kb = new KeyMapping("key.tsunderify.transform", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.category.tsunderify.chat");
        *///?}

        KeyBindingHelper.registerKeyBinding(kb);
        return kb;
    }
}
