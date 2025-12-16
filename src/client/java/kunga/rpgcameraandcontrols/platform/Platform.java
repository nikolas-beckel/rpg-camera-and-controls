package kunga.rpgcameraandcontrols.platform;

import kunga.rpgcameraandcontrols.platform.input.PlatformInput;
import net.minecraft.client.MinecraftClient;

public final class Platform {
    private static PlatformInput input;

    public static void initialize(MinecraftClient client) {
        input = new PlatformInput(client);
    }

    public static PlatformInput input() {
        if (input == null) {
            throw new IllegalStateException("Platform not initialized! Call Platform.initialize() first.");
        }
        return input;
    }
}
