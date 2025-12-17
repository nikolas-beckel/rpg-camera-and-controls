package kunga.rpgcameraandcontrols.target;

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
            LOGGER.info("Neues Ziel anvisiert: " + currentTarget.getName().getString());
        } else {
            LOGGER.info("Ziel aufgehoben (Nichts anvisiert).");
        }
    }

    public static Entity getTarget() {
        return currentTarget;
    }

    public static boolean hasTarget() {
        return currentTarget != null;
    }
}