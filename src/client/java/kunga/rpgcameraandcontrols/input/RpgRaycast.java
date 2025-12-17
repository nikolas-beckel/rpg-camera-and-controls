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
        Window win = client.getWindow();
        double mx = mouse.getX();
        double my = mouse.getY();

        // Normalisierte Gerätekoordinaten
        float fx = (float) (mx * 2.0 / win.getFramebufferWidth() - 1.0f);
        float fy = (float) (1.0 - my * 2.0 / win.getFramebufferHeight());

        Camera camera = client.gameRenderer.getCamera();
        Vec3d start = camera.getCameraPos();
        Vec3d dir = camera.getProjection().getPosition(fx, fy).normalize();

        // Maximale Reichweite für das Anvisieren (kann angepasst werden)
        double maxRange = 100.0;
        Vec3d end = start.add(dir.multiply(maxRange));

        // 1. Block Raycast
        BlockHitResult blockHit = client.world.raycast(new RaycastContext(
            start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE,
            client.getCameraEntity()));

        double blockDistSq = blockHit.getType() == HitResult.Type.MISS
            ? Double.POSITIVE_INFINITY
            : blockHit.getPos().squaredDistanceTo(start);

        // 2. Entity Raycast
        Entity camEntity = client.getCameraEntity();
        Box sweep = camEntity.getBoundingBox().stretch(dir.multiply(maxRange)).expand(1.0, 1.0, 1.0);

        EntityHitResult entHit = ProjectileUtil.raycast(
            camEntity, start, end, sweep, EntityPredicates.CAN_HIT, maxRange * maxRange);

        if (entHit != null) {
            double entDistSq = entHit.getPos().squaredDistanceTo(start);
            // Wenn Entity näher ist als der Block (oder kein Block getroffen wurde)
            if (entDistSq < blockDistSq) {
                return entHit;
            }
        }
        return blockHit;
    }
}