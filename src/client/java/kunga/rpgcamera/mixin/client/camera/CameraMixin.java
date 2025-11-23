package kunga.rpgcamera.mixin.client.camera;

import net.minecraft.world.BlockView;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import kunga.rpgcamera.input.RpgCameraInput;
import kunga.rpgcamera.util.ClientUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

@Mixin(Camera.class)
public final class CameraMixin {
    @Unique
    private Camera self = (Camera) (Object) this;

    @Unique
    private CameraAccessor accessor;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initialization(CallbackInfo ci) {
        this.accessor = (CameraAccessor) self;
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void rpg$update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inversiveView,
            float tickProgress, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (!ClientUtil.isIngame(client) || !ClientUtil.isRpgThirdPerson(client))
            return;

        RpgCameraInput.ensureDefaultCameraPosition();

        // When moving and not orbiting set camera back behind player.
        if (!RpgCameraInput.isOrbiting()) {
            var moving = focusedEntity.getVelocity().horizontalLengthSquared() > 0.0001;

            var yawNowDeg = focusedEntity.getYaw(tickProgress);
            var yawLastDeg = focusedEntity.lastYaw;
            var turning = Math.abs(MathHelper.wrapDegrees(yawNowDeg - yawLastDeg)) > 0.5f;

            if (moving || turning) {
                var desiredOrbitYaw = Math.toRadians(yawNowDeg);

                RpgCameraInput.nudgeYawTowards(desiredOrbitYaw, 0.12);
            }
        }

        final double targetX = MathHelper.lerp(tickProgress, focusedEntity.lastX, focusedEntity.getX());
        final double targetY = MathHelper.lerp(tickProgress, focusedEntity.lastY, focusedEntity.getY())
                + focusedEntity.getStandingEyeHeight() * 0.8;
        final double targetZ = MathHelper.lerp(tickProgress, focusedEntity.lastZ, focusedEntity.getZ());

        float camYawDeg = (float) Math.toDegrees(RpgCameraInput.getOrbitYawRadians());
        float camPitchDeg = (float) Math.toDegrees(RpgCameraInput.getOrbitPitchRadians());
        if (inversiveView) {
            camYawDeg += 180.0f;
            camPitchDeg = -camPitchDeg;
        }
        this.accessor.invokeSetRotation(camYawDeg, camPitchDeg);

        final double radius = RpgCameraInput.getRadiusForFrame();
        Vector3f fwd = self.getHorizontalPlane(); // Blickrichtung (normiert)
        Vec3d desired = new Vec3d(
                targetX - fwd.x * radius,
                targetY - fwd.y * radius,
                targetZ - fwd.z * radius);

        Vec3d from = new Vec3d(targetX, targetY, targetZ);
        Vec3d to = desired;

        if (client != null && client.world != null) {
            RaycastContext ctx = new RaycastContext(
                    from, to,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    focusedEntity);
            BlockHitResult hit = client.world.raycast(ctx);
            if (hit.getType() != HitResult.Type.MISS) {
                double eps = 0.3;
                Vec3d hp = hit.getPos();
                this.accessor.invokeSetPos(hp.x + fwd.x * eps, hp.y + fwd.y * eps, hp.z + fwd.z * eps);
                return;
            }
        }

        this.accessor.invokeSetPos(desired.x, desired.y, desired.z);
    }
}
