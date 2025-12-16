package kunga.rpgcameraandcontrols.platform.input;

import net.minecraft.client.MinecraftClient;

public class PlatformInput implements PlatformAgnosticInput {
    private final MinecraftClient client;

    public PlatformInput(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public boolean isControlDown() {
        return this.client.isCtrlPressed();
    }
}
