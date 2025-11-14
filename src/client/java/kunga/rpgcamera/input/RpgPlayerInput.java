package kunga.rpgcamera.input;

import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;

public final class RpgPlayerInput {
    private static double TURN_SPEED_IN_ANGULAR_PER_SEC = 180;

    private static PlayerInput playerInput = PlayerInput.DEFAULT;
    private static boolean currentTurnLeft = false;
    private static boolean currentTurnRight = false;

    private RpgPlayerInput() {
    }

    public static void setMovement(
        boolean forward,
        boolean backward,
        boolean strafeLeft,
        boolean turnLeft,
        boolean strafeRight,
        boolean turnRight,
        boolean jump,
        boolean sneak,
        boolean sprint
    ) {
        playerInput = new PlayerInput(forward, backward, strafeLeft, strafeRight, jump, sneak, sprint);
        currentTurnLeft = turnLeft;
        currentTurnRight = turnRight;
    }

    public static PlayerInput getPlayerInput() {
        return playerInput;
    }

    public static Vec2f getMovementVector() {
        var forward = getMovementMultiplier(playerInput.forward(), playerInput.backward());
        var sideways = getMovementMultiplier(playerInput.left(), playerInput.right());
        return (forward == 0.0f && sideways == 0.0f) ? Vec2f.ZERO : new Vec2f(sideways, forward).normalize();
    }

    public static double getTurnSpeedInAngularPerSecond() {
        var turnDirection = (currentTurnLeft ? -1 : 0) + (currentTurnRight ? 1 : 0);
        if (playerInput.sprint()) TURN_SPEED_IN_ANGULAR_PER_SEC *= 1.2;
        if (playerInput.sneak()) TURN_SPEED_IN_ANGULAR_PER_SEC *= 0.6;

        return turnDirection == 0 ? 0.0 : turnDirection * TURN_SPEED_IN_ANGULAR_PER_SEC;
    }

    private static float getMovementMultiplier(boolean positive, boolean negative) {
        return (float) Boolean.compare(positive, negative);
    }
}
