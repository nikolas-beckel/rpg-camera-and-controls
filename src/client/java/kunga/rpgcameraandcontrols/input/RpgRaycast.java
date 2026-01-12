package kunga.rpgcameraandcontrols.input;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class RpgRaycast {

    public static HitResult pickUnderMouse(Mouse mouse, MinecraftClient client) {
        if (client.world == null) return null;

        Window window = client.getWindow();

        // Mouse-Koordinaten in Fensterkoordinaten
        double mouseXInWindowPixels = mouse.getX();
        double mouseYInWindowPixels = mouse.getY();

        // Faktoren für Camera.Projection.getPosition:
        // - factorX: -1 links ... +1 rechts
        // - factorY: +1 oben ... -1 unten
        float projectionFactorX = (float) (mouseXInWindowPixels * 2.0 / window.getWidth() - 1.0);
        float projectionFactorY = (float) (1.0 - mouseYInWindowPixels * 2.0 / window.getHeight());

        Camera camera = client.gameRenderer.getCamera();

        // Startpunkt ist die Render-Kamera-Position (Third-Person hinter dem Spieler)
        Vec3d rayStartWorldPosition = camera.getCameraPos();

        // Richtung aus der Kamera-Projektion (FOV/Aspect/Rotation) -> das ist echtes Screen-Picking
        Vec3d rayDirectionWorld = camera.getProjection()
            .getPosition(projectionFactorX, projectionFactorY)
            .normalize();

        // Reichweite (zum Debuggen ruhig hoch lassen)
        double maxRange = 100.0;
        Vec3d rayEndWorldPosition = rayStartWorldPosition.add(rayDirectionWorld.multiply(maxRange));

        // 1) Block Raycast
        BlockHitResult blockHit = client.world.raycast(new RaycastContext(
            rayStartWorldPosition,
            rayEndWorldPosition,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            client.getCameraEntity()
        ));

        double blockHitDistanceSquared = blockHit.getType() == HitResult.Type.MISS
            ? Double.POSITIVE_INFINITY
            : blockHit.getPos().squaredDistanceTo(rayStartWorldPosition);

        // 2) Entity Raycast
        Entity cameraEntity = client.getCameraEntity();

        // WICHTIG: Sweep-Box entlang des Rays bauen (nicht von der Player-Boundingbox aus!)
        Box sweepBoxAlongRay = new Box(rayStartWorldPosition, rayEndWorldPosition).expand(1.0, 1.0, 1.0);

        EntityHitResult entityHit = ProjectileUtil.raycast(
            cameraEntity,
            rayStartWorldPosition,
            rayEndWorldPosition,
            sweepBoxAlongRay,
            EntityPredicates.CAN_HIT,
            Math.min(blockHitDistanceSquared, maxRange * maxRange)
        );

        if (entityHit != null) {
            return entityHit;
        }

        return blockHit;
    }
}
