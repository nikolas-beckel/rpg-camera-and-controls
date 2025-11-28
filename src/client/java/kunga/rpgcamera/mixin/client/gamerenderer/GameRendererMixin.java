package kunga.rpgcamera.mixin.client.gamerenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import kunga.rpgcamera.input.RpgCameraInput;
import kunga.rpgcamera.input.RpgMouseInput;
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
    private void initialize(MinecraftClient client, HeldItemRenderer heldItemRenderer,
            BufferBuilderStorage bufferBuilderStorage, CallbackInfo ci) {
        this.accessor = (GameRendererAccessor) self;
    }

    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"))
    private void rpg$shouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        if (RpgCameraInput.isOrbiting()) {
            return;
        }

        var client = self.getClient();
        var camera = self.getCamera();

        if (client == null || client.world == null || client.getCameraEntity() == null) {
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

        if (!RpgMouseInput.isHitResultWithinPlayerReach(client.player, blockHit)) {
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
    private void tutorialmod$updateCrosshairTarget(float tickProgress, CallbackInfo ci) {
        var entity = self.getClient().getCameraEntity();
        if (entity != null) {
            if (self.getClient().world != null && self.getClient().player != null) {
                var hitResult = rpg$pickUnderMouse(self.getClient().mouse, self.getClient());

                if (RpgMouseInput.isHitResultWithinPlayerReach(self.getClient().player, hitResult)
                        && !RpgCameraInput.isOrbiting()) {
                    self.getClient().crosshairTarget = hitResult;
                    self.getClient().targetedEntity = hitResult instanceof EntityHitResult entityHitResult
                            ? entityHitResult.getEntity()
                            : null;
                    ci.cancel();
                    return;
                }

                self.getClient().crosshairTarget = null;
                self.getClient().targetedEntity = null;
                ci.cancel();
                return;
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
