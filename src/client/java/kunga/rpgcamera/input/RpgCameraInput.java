package kunga.rpgcamera.input;

public final class RpgCameraInput {

    private static boolean isLeftMouseHeld = false;

    private static boolean hasCalibration = false;

    private static double orbitYawRadians = 0;
    private static double orbitPitchRadians = Math.toRadians(20.0);
    private static double orbitRadius = 4.0;

    private static final double MIN_PITCH = Math.toRadians(-85.0);
    private static final double MAX_PITCH = Math.toRadians(85.0);

    private static double yawRadiansPerPixel = 0.0015;
    private static double pitchRadiansPerPixel = 0.0011;

    private static boolean movedDuringHold = false;

    private static double accumulatedDx = 0.0;
    private static double accumulatedDy = 0.0;

    private static final double dragThresholdPixelsSquared = 2000.0;

    private RpgCameraInput() {
    }

    public static void onLeftMouseDown() {
        isLeftMouseHeld = true;
        movedDuringHold = false;
        accumulatedDx = 0.0;
        accumulatedDy = 0.0;
    }

    public static void onLeftMouseUp() {
        isLeftMouseHeld = false;
        accumulatedDx = 0.0;
        accumulatedDy = 0.0;
    }

    public static boolean isOrbiting() {
        return isLeftMouseHeld;
    }

    public static void ensureDefaultCameraPosition() {
        if (!hasCalibration)
            setDefaultCameraPosition();
    }

    private static void setDefaultCameraPosition() {
        orbitYawRadians = Math.toRadians(0);
        orbitPitchRadians = Math.toRadians(30);
        orbitRadius = 12.0;

        hasCalibration = true;
    }

    public static void calculateOrbitRadiants(double deltaX, double deltaY) {
        accumulatedDx += deltaX;
        accumulatedDy += deltaY;
        double distSq = (accumulatedDx * accumulatedDx) + (accumulatedDy * accumulatedDy);
        if (distSq >= dragThresholdPixelsSquared) {
            movedDuringHold = true;
        }

        if (!isLeftMouseHeld) {
            return;
        }

        if (Math.abs(deltaX) < 0.01 && Math.abs(deltaY) < 0.01) {
            return;
        }

        orbitYawRadians += deltaX * yawRadiansPerPixel;
        orbitPitchRadians = Math.clamp(orbitPitchRadians + deltaY * pitchRadiansPerPixel, MIN_PITCH, MAX_PITCH);
    }

    public static double getRadiusForFrame() {
        try {
            double zoomR = RpgMouseInput.getCurrentZoom();
            if (Double.isFinite(zoomR) && zoomR > 0.0) {
                orbitRadius = zoomR;
            }
        } catch (Throwable ignored) {
            // TODO: Hier mit catch arbeiten.
        }

        return orbitRadius;
    }

    private static double wrapPi(double a) {
        while (a <= -Math.PI)
            a += Math.PI * 2.0;
        while (a > Math.PI)
            a -= Math.PI * 2.0;
        return a;
    }

    private static double lerpAngle(double from, double to, double t) {
        double diff = wrapPi(to - from);
        return wrapPi(from + diff * t);
    }

    public static void nudgeYawTowards(double desiredYawRadians, double blend) {
        orbitYawRadians = lerpAngle(orbitYawRadians, wrapPi(desiredYawRadians), blend);
    }

    public static double getOrbitYawRadians() {
        return orbitYawRadians;
    }

    public static double getOrbitPitchRadians() {
        return orbitPitchRadians;
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
