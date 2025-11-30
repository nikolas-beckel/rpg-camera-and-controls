package kunga.rpgcameraandcontrols.input;

import net.minecraft.client.MinecraftClient;

public final class UseKeyInput {

    private static boolean wasPressed = false;
    private static boolean isPressed = false;
    private static boolean justPressed = false;
    private static boolean justReleased = false;

    public static void tick(MinecraftClient client) {
        boolean currentlyPressed = client.options.useKey.isPressed();

        justPressed = !wasPressed && currentlyPressed;
        justReleased = wasPressed && !currentlyPressed;
        isPressed = currentlyPressed;
        wasPressed = currentlyPressed;
    }

    public static boolean isJustPressed() {
        return justPressed;
    }

    public static boolean isJustReleased() {
        return justReleased;
    }

    public static boolean isHeld() {
        return isPressed;
    }

    public static void reset() {
        wasPressed = false;
        isPressed = false;
        justPressed = false;
        justReleased = false;
    }
}