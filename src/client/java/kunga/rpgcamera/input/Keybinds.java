package kunga.rpgcamera.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class Keybinds {
    public static final String CATEGORY_MOVEMENT = "category.rpg-camera.controls";

    public static KeyBinding TURN_LEFT_KEY;
    public static KeyBinding TURN_RIGHT_KEY;
    public static KeyBinding STRAFE_LEFT_KEY;
    public static KeyBinding STRAFE_RIGHT_KEY;
    public static KeyBinding DROP_ITEM;
    public static KeyBinding OPEN_INVENTORY;

    public static void register() {
        TURN_LEFT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rpg-camera.turn_left",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_A,
            CATEGORY_MOVEMENT
        ));

        TURN_RIGHT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rpg-camera.turn_right",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_D,
            CATEGORY_MOVEMENT
        ));

        STRAFE_LEFT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rpg-camera.strafe_left",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Q,
            CATEGORY_MOVEMENT
        ));

        STRAFE_RIGHT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rpg-camera.strafe_right",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            CATEGORY_MOVEMENT
        ));

        DROP_ITEM = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rpg-camera.drop_item",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            CATEGORY_MOVEMENT
        ));

        OPEN_INVENTORY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rpg-camera.open_inventory",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY_MOVEMENT
        ));
    }

    public static boolean turnLeftKeyIsPressed(MinecraftClient client) {
        return InputUtil.isKeyPressed(
            client.getWindow().getHandle(),
            Keybinds.TURN_LEFT_KEY.getDefaultKey().getCode()
        );
    }

    public static boolean turnRightKeyIsPressed(MinecraftClient client) {
        return InputUtil.isKeyPressed(
            client.getWindow().getHandle(),
            Keybinds.TURN_RIGHT_KEY.getDefaultKey().getCode()
        );
    }
}
