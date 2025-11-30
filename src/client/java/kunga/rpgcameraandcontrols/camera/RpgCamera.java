package kunga.rpgcameraandcontrols.camera;

import kunga.rpgcameraandcontrols.RpgCameraAndControls;
import kunga.rpgcameraandcontrols.input.RpgMouseInput;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class RpgCamera {

    private static final Identifier CAMERA_ZOOM_ID = Identifier.of(RpgCameraAndControls.MOD_ID, "camera-zoom");

    public static double ZOOM_MIN = 1.5;
    public static double ZOOM_MAX = 12.0;
    public static double ZOOM_STEP = 0.5;
    public static double ZOOM_SMOOTHING = 10;

    private static double targetZoom = Double.NaN;
    private static double currentZoom = Double.NaN;

    private static boolean hasCalibration = false;
    private static double orbitYawRadians = 0;
    private static double orbitPitchRadians = Math.toRadians(20.0);
    private static double orbitRadius = 4.0;

    private static final double MIN_PITCH = Math.toRadians(-85.0);
    private static final double MAX_PITCH = Math.toRadians(85.0);

    private RpgCamera() {
    }

    public static boolean isOrbiting() {
        return RpgMouseInput.isRightMouseHeld();
    }

    public static void ensureDefaultCameraPosition() {
        if (!hasCalibration) {
            setDefaultCameraPosition();
        }
    }

    private static void setDefaultCameraPosition() {
        orbitYawRadians = Math.toRadians(0);
        orbitPitchRadians = Math.toRadians(30);
        orbitRadius = 12.0;
        hasCalibration = true;
    }

    public static void updateOrbitFromMouseDelta() {
        if (!RpgMouseInput.isRightMouseHeld()) {
            return;
        }

        double deltaX = RpgMouseInput.getCurrentDeltaX();
        double deltaY = RpgMouseInput.getCurrentDeltaY();

        if (Math.abs(deltaX) < 0.01 && Math.abs(deltaY) < 0.01) {
            return;
        }

        double yawRadiansPerPixel = 0.0015;
        double pitchRadiansPerPixel = 0.0011;
        orbitYawRadians += deltaX * yawRadiansPerPixel;
        orbitPitchRadians = Math.clamp(orbitPitchRadians + deltaY * pitchRadiansPerPixel, MIN_PITCH, MAX_PITCH);
    }

    public static double getOrbitYawRadians() {
        return orbitYawRadians;
    }

    public static double getOrbitPitchRadians() {
        return orbitPitchRadians;
    }

    public static void nudgeYawTowards(double desiredYawRadians, double blend) {
        orbitYawRadians = lerpAngle(orbitYawRadians, wrapPi(desiredYawRadians), blend);
    }

    public static double getCurrentZoom() {
        return currentZoom;
    }

    public static double getRadiusForFrame() {
        try {
            if (Double.isFinite(currentZoom) && currentZoom > 0.0) {
                orbitRadius = currentZoom;
            }
        } catch (Throwable ignored) {
            // TODO: Do something?
        }
        return orbitRadius;
    }

    public static void applyScrollZoom(PlayerEntity player) {
        double scrollDelta = RpgMouseInput.consumeScrollDelta();
        if (scrollDelta == 0.0) {
            return;
        }

        var cameraDistanceAttribute = player.getAttributeInstance(EntityAttributes.CAMERA_DISTANCE);
        if (cameraDistanceAttribute == null) {
            return;
        }

        cameraDistanceAttribute.addTemporaryModifier(initializeAttributes(cameraDistanceAttribute));
        targetZoom = MathHelper.clamp(targetZoom - scrollDelta * ZOOM_STEP, ZOOM_MIN, ZOOM_MAX);
    }

    public static void updateZoom(PlayerEntity player, double deltaSeconds) {
        var cameraDistanceAttribute = player.getAttributeInstance(EntityAttributes.CAMERA_DISTANCE);
        if (cameraDistanceAttribute == null) {
            return;
        }

        cameraDistanceAttribute.addTemporaryModifier(initializeAttributes(cameraDistanceAttribute));

        double alpha = 1.0 - Math.exp(-ZOOM_SMOOTHING * deltaSeconds);
        currentZoom += (targetZoom - currentZoom) * alpha;

        cameraDistanceAttribute.addTemporaryModifier(applyAttributes(cameraDistanceAttribute, currentZoom));
    }

    public static boolean isHitResultWithinPlayerReach(PlayerEntity player, HitResult hitResult) {
        if (player == null || hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
            return false;
        }

        Vec3d playerEyePos = player.getCameraPosVec(1.0f);

        final double allowedBlockReach = player.getBlockInteractionRange();
        final double allowedEntityReach = player.getEntityInteractionRange();

        switch (hitResult.getType()) {
            case BLOCK: {
                BlockHitResult blockHit = (BlockHitResult) hitResult;
                Vec3d blockHitPosWorld = new Vec3d(blockHit.getPos().x, blockHit.getPos().y, blockHit.getPos().z);
                double distance = playerEyePos.distanceTo(blockHitPosWorld);
                return distance <= (allowedBlockReach + 0.05);
            }
            case ENTITY: {
                EntityHitResult entityHit = (EntityHitResult) hitResult;
                Entity target = entityHit.getEntity();
                if (target == null) {
                    return false;
                }
                Vec3d entityPosWorld = target.getPos();
                double distance = playerEyePos.distanceTo(entityPosWorld);
                return distance <= (allowedEntityReach + 0.05);
            }
            default:
                return false;
        }
    }

    private static EntityAttributeModifier initializeAttributes(EntityAttributeInstance cameraDistanceAttribute) {
        if (!Double.isNaN(targetZoom) && !Double.isNaN(currentZoom)) {
            return applyAttributes(cameraDistanceAttribute, currentZoom);
        }

        cameraDistanceAttribute.removeModifier(CAMERA_ZOOM_ID);
        var defaultValue = cameraDistanceAttribute.getValue();
        targetZoom = defaultValue;
        currentZoom = defaultValue;

        return applyAttributes(cameraDistanceAttribute, currentZoom);
    }

    private static EntityAttributeModifier applyAttributes(EntityAttributeInstance cameraDistanceAttribute,
                                                           double desiredZoom) {
        desiredZoom = MathHelper.clamp(desiredZoom, ZOOM_MIN, ZOOM_MAX);

        cameraDistanceAttribute.removeModifier(CAMERA_ZOOM_ID);
        var defaultValue = cameraDistanceAttribute.getValue();
        var offsetZoom = desiredZoom - defaultValue;

        return new EntityAttributeModifier(
            CAMERA_ZOOM_ID,
            offsetZoom,
            EntityAttributeModifier.Operation.ADD_VALUE);
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

    public static boolean hasMovedDuringHold() {
        return RpgMouseInput.hasMovedDuringHold();
    }

    public static boolean consumeMovedFlagOnRelease() {
        return RpgMouseInput.consumeMovedFlagOnRelease();
    }
}