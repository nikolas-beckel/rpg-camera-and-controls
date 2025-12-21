package kunga.rpgcameraandcontrols;

import kunga.rpgcameraandcontrols.network.TargetC2SPayload;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpgCameraAndControls implements ModInitializer {
	public static final String MOD_ID = "rpg-camera";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

        PayloadTypeRegistry.playC2S().register(TargetC2SPayload.ID, TargetC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TargetC2SPayload.ID, ((payload, context) -> {
            var targetId = payload.targetId();
            LOGGER.info("Received TargetC2SPayload Entity ID: {}", payload.targetId());

            var player = context.player();

            // Auf dem Server-Thread ausführen
            context.server().execute(() -> {
                var target = player.getWorld().getEntityById(targetId);
                if (target != null) {
                    RpgCameraAndControls.LOGGER.info("Player {} targeted entity {}", player.getName().getString(), target.getName().getString());
                }
            });
        }));
	}
}