package kunga.rpgcamera.model;

import kunga.rpgcamera.input.RpgPlayerInput;
import net.minecraft.util.math.MathHelper;

public final class PlayerHead {
    private static final double MAX_YAW_DEG = 32.0;
    private static final double SMOOTHING_SPEED = 10.0;

    private static float currentYawRadiant = 0.0F;

    public static void update(double deltaSeconds) {
        var turnSpeedInDegreesPerSecond = RpgPlayerInput.getTurnSpeedInDegreesPerSecond();
        var normalizeSpeed = MathHelper.clamp(
            turnSpeedInDegreesPerSecond / RpgPlayerInput.TURN_SPEED_IN_DEGREE_PER_SEC,
            -1.0,
            1.0
        );
        var targetYawRadiant = Math.toRadians(MAX_YAW_DEG) * normalizeSpeed;
        var alpha = 1.0 - Math.exp(-SMOOTHING_SPEED * deltaSeconds);
        currentYawRadiant += (float) ((targetYawRadiant - currentYawRadiant) * alpha);
    }

    public static float getCurrentYawRadiant() {
        return currentYawRadiant;
    }
}
