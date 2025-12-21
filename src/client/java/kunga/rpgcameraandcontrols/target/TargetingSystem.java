package kunga.rpgcameraandcontrols.target;

import kunga.rpgcameraandcontrols.network.TargetC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TargetingSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger("RpgTargeting");
    private static Entity currentTarget;

    public static void setTarget(Entity entity) {
        // Wenn sich das Ziel nicht geändert hat, nichts tun (optional)
        if (currentTarget == entity) return;

        currentTarget = entity;

        if (currentTarget != null) {
            LOGGER.info("New target: {}", currentTarget.getName().getString());
            ClientPlayNetworking.send(new TargetC2SPayload(currentTarget.getId()));
        } else {
            LOGGER.info("Target reset / no target.");
        }
    }

    public static Entity getTarget() {
        return currentTarget;
    }

    public static boolean hasTarget() {
        return currentTarget != null;
    }
}