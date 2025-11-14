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
    public static final String CATEGORY_RPG_CAMERA_AND_CONTROLS = "category.rpg-camera.rpg-camera-and-controls";

    public static KeyBinding TURN_LEFT_KEY;
    public static KeyBinding TURN_RIGHT_KEY;
    public static KeyBinding STRAFE_LEFT_KEY;
    public static KeyBinding STRAFE_RIGHT_KEY;
    public static KeyBinding DROP_ITEM;
    public static KeyBinding OPEN_INVENTORY;
    public static KeyBinding TOGGLE_ZOOM_KEY;

    public static void register() {
        TURN_LEFT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rpg-camera.turn_left",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_A,
            CATEGORY_RPG_CAMERA_AND_CONTROLS
        ));

        TURN_RIGHT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rpg-camera.turn_right",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_D,
            CATEGORY_RPG_CAMERA_AND_CONTROLS
        ));

        STRAFE_LEFT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rpg-camera.strafe_left",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Q,
            CATEGORY_RPG_CAMERA_AND_CONTROLS
        ));

        STRAFE_RIGHT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rpg-camera.strafe_right",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            CATEGORY_RPG_CAMERA_AND_CONTROLS
        ));

        DROP_ITEM = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rpg-camera.drop_item",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            CATEGORY_RPG_CAMERA_AND_CONTROLS
        ));

        OPEN_INVENTORY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rpg-camera.open_inventory",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY_RPG_CAMERA_AND_CONTROLS
        ));

        TOGGLE_ZOOM_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rpg-camera.toggle_zoom",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            CATEGORY_RPG_CAMERA_AND_CONTROLS
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
