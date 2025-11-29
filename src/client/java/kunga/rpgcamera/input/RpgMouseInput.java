package kunga.rpgcamera.input;

public final class RpgMouseInput {

    private static boolean isRightMouseHeld = false;

    private static double accumulatedDeltaX = 0.0;
    private static double accumulatedDeltaY = 0.0;

    private static double currentDeltaX = 0.0;
    private static double currentDeltaY = 0.0;

    private static double scrollDelta = 0.0;

    private static final double DRAG_THRESHOLD_PIXELS_SQUARED = 2000.0;
    private static boolean movedDuringHold = false;

    public static void onRightMouseDown() {
        isRightMouseHeld = true;
        movedDuringHold = false;
        accumulatedDeltaX = 0.0;
        accumulatedDeltaY = 0.0;
    }

    public static void onRightMouseUp() {
        isRightMouseHeld = false;
        accumulatedDeltaX = 0.0;
        accumulatedDeltaY = 0.0;
    }

    public static boolean isRightMouseHeld() {
        return isRightMouseHeld;
    }

    public static void updateCursorDelta(double deltaX, double deltaY) {
        currentDeltaX = deltaX;
        currentDeltaY = deltaY;

        accumulatedDeltaX += deltaX;
        accumulatedDeltaY += deltaY;

        double distSq = (accumulatedDeltaX * accumulatedDeltaX) + (accumulatedDeltaY * accumulatedDeltaY);
        if (distSq >= DRAG_THRESHOLD_PIXELS_SQUARED) {
            movedDuringHold = true;
        }
    }

    public static double getCurrentDeltaX() {
        return currentDeltaX;
    }

    public static double getCurrentDeltaY() {
        return currentDeltaY;
    }

    public static void addScrollDelta(double vertical) {
        scrollDelta += vertical;
    }

    public static double consumeScrollDelta() {
        double delta = scrollDelta;
        scrollDelta = 0.0;
        return delta;
    }

    public static boolean hasMovedDuringHold() {
        return movedDuringHold;
    }

    public static boolean consumeMovedFlagOnRelease() {
        boolean moved = movedDuringHold;
        movedDuringHold = false;
        return moved;
    }
}