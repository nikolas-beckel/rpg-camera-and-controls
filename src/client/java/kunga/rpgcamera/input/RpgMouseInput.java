package kunga.rpgcamera.input;

import kunga.rpgcamera.RPGCamera;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public final class RpgMouseInput {
    private static final Identifier CAMERA_ZOOM_ID = Identifier.of(RPGCamera.MOD_ID, "camera-zoom");

    public static double MIN = 1.5;
    public static double MAX = 12.0;
    public static double STEP = 0.5;
    public static double SMOOTHING = 10;

    private static double targetZoom = Double.NaN;
    private static double currentZoom = Double.NaN;

    public static void nudgeByScroll(PlayerEntity player, double vertical) {
        var cameraDistanceAttribute = player.getAttributeInstance(EntityAttributes.CAMERA_DISTANCE);
        if (cameraDistanceAttribute == null) return;

        cameraDistanceAttribute.addTemporaryModifier(initializeAttributes(cameraDistanceAttribute));

        targetZoom = MathHelper.clamp(targetZoom - vertical * STEP, MIN, MAX);
    }

    public static void update(PlayerEntity player, double deltaSeconds) {
        var cameraDistanceAttribute = player.getAttributeInstance(EntityAttributes.CAMERA_DISTANCE);
        if (cameraDistanceAttribute == null) return;

        cameraDistanceAttribute.addTemporaryModifier(initializeAttributes(cameraDistanceAttribute));

        double alpha = 1.0 - Math.exp(-SMOOTHING * deltaSeconds);
        currentZoom += (targetZoom - currentZoom) * alpha;

        cameraDistanceAttribute.addTemporaryModifier(applyAttributes(cameraDistanceAttribute, currentZoom));
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

    private static EntityAttributeModifier applyAttributes(EntityAttributeInstance cameraDistanceAttribute, double desiredZoom) {
        desiredZoom = MathHelper.clamp(desiredZoom, MIN, MAX);

        cameraDistanceAttribute.removeModifier(CAMERA_ZOOM_ID);
        var defaultValue = cameraDistanceAttribute.getValue();
        var offsetZoom = desiredZoom - defaultValue;

        return new EntityAttributeModifier(
            CAMERA_ZOOM_ID,
            offsetZoom,
            EntityAttributeModifier.Operation.ADD_VALUE
        );
    }
}
