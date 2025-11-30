package kunga.rpgcameraandcontrols;

import kunga.rpgcameraandcontrols.input.Keybinds;
import net.fabricmc.api.ClientModInitializer;

public class RpgCameraAndControlsClient implements ClientModInitializer {

    @Override
	public void onInitializeClient() {
        Keybinds.register();
	}

}