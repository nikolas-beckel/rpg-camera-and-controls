package kunga.rpgcamera;

import kunga.rpgcamera.input.Keybinds;
import net.fabricmc.api.ClientModInitializer;

public class RPGCameraClient implements ClientModInitializer {

    @Override
	public void onInitializeClient() {
        Keybinds.register();
	}

}