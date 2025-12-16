package kunga.rpgcameraandcontrols;

import kunga.rpgcameraandcontrols.input.Keybinds;
import kunga.rpgcameraandcontrols.platform.Platform;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class RpgCameraAndControlsClient implements ClientModInitializer {

    @Override
	public void onInitializeClient() {
        var client = MinecraftClient.getInstance();

        Platform.initialize(client);
        Keybinds.register();
	}

}