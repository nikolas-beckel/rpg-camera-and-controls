package kunga.rpgcamera.mixin.client.gamerenderer;

import kunga.rpgcamera.util.ClientUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import kunga.rpgcamera.camera.RpgCamera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

@Mixin(GameRenderer.class)
public final class GameRendererMixin {

    @Unique
    GameRenderer self = (GameRenderer) (Object) this;

    @Unique
    GameRendererAccessor accessor;

    @Unique
    private static HitResult crosshairTarget;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initialize(MinecraftClient client, HeldItemRenderer heldItemRenderer, BufferBuilderStorage bufferBuilderStorage, CallbackInfo ci) {
        this.accessor = (GameRendererAccessor) self;
    }

    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"))
    private void rpg$shouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        var client = self.getClient();
        if (!ClientUtil.isRpgThirdPerson(client)) {
            return;
        }

        if (RpgCamera.isOrbiting()) {
            return;
        }

        var camera = self.getCamera();

        if (client.world == null || client.getCameraEntity() == null) {
            return;
        }

        if (client.currentScreen != null || client.getOverlay() != null) {
            return;
        }

        if (client.mouse.isCursorLocked()) {
            return;
        }

        Window win = client.getWindow();
        double mx = client.mouse.getX();
        double my = client.mouse.getY();
        float fx = (float) (mx * 2.0 / win.getFramebufferWidth() - 1.0f);
        float fy = (float) (1.0 - my * 2.0 / win.getFramebufferHeight());

        Vec3d start = camera.getCameraPos();
        Vec3d dir = camera.getProjection().getPosition(fx, fy).normalize();
        double maxRange = 1024.0;
        Vec3d end = start.add(dir.multiply(maxRange));

        BlockHitResult blockHit = client.world.raycast(
                new RaycastContext(
                        start,
                        end,
                        RaycastContext.ShapeType.OUTLINE,
                        RaycastContext.FluidHandling.NONE,
                        client.getCameraEntity()));

        if (!RpgCamera.isHitResultWithinPlayerReach(client.player, blockHit)) {
            return;
        }

        client.crosshairTarget = blockHit != null
                ? blockHit
                : BlockHitResult.createMissed(
                        start,
                        Direction.getFacing(dir.x, dir.y, dir.z),
                        BlockPos.ofFloored(start));

        client.targetedEntity = null;
    }

    @Inject(method = "updateCrosshairTarget(F)V", at = @At("HEAD"), cancellable = true)
    private void rpg$updateCrosshairTarget(float tickProgress, CallbackInfo ci) {
        var client = self.getClient();
        if (!ClientUtil.isRpgThirdPerson(client)) {
            return;
        }

        var entity = client.getCameraEntity();
        if (entity != null) {
            if (client.world != null && client.player != null) {
                var hitResult = rpg$pickUnderMouse(client.mouse, client);

                if (RpgCamera.isHitResultWithinPlayerReach(client.player, hitResult)
                        && !RpgCamera.isOrbiting()) {
                    client.crosshairTarget = hitResult;
                    client.targetedEntity = hitResult instanceof EntityHitResult entityHitResult
                            ? entityHitResult.getEntity()
                            : null;
                    ci.cancel();
                    return;
                }

                client.crosshairTarget = null;
                client.targetedEntity = null;
                ci.cancel();
            }
        }
    }

    @Unique
    private static HitResult rpg$pickUnderMouse(Mouse mouseInstance, MinecraftClient client) {
        Window win = client.getWindow();
        double mx = mouseInstance.getX();
        double my = mouseInstance.getY();

        float fx = (float) (mx * 2.0 / win.getFramebufferWidth() - 1.0f);
        float fy = (float) (1.0 - my * 2.0 / win.getFramebufferHeight());

        GameRenderer gr = client.gameRenderer;
        Camera camera = gr.getCamera();

        Vec3d start = camera.getCameraPos();
        Vec3d dir = camera.getProjection().getPosition(fx, fy).normalize();
        double maxRange = 1024.0;
        Vec3d end = start.add(dir.multiply(maxRange));

        BlockHitResult blockHit = client.world.raycast(new RaycastContext(
                start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE,
                client.getCameraEntity()));
        double blockDistSq = blockHit.getType() == HitResult.Type.MISS
                ? Double.POSITIVE_INFINITY
                : blockHit.getPos().squaredDistanceTo(start);

        Entity camEntity = client.getCameraEntity();
        Box sweep = camEntity.getBoundingBox().stretch(dir.multiply(maxRange)).expand(1.0, 1.0, 1.0);
        EntityHitResult entHit = ProjectileUtil.raycast(
                camEntity, start, end, sweep, EntityPredicates.CAN_HIT, maxRange * maxRange);

        if (entHit != null) {
            double entDistSq = entHit.getPos().squaredDistanceTo(start);
            if (entDistSq < blockDistSq) {
                return entHit;
            }
        }
        return blockHit;
    }
}
