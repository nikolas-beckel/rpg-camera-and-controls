package kunga.rpgcamera.mixin.client.minecraftclient;

import kunga.rpgcamera.input.RpgMouseInput;
import kunga.rpgcamera.input.RpgPlayerInput;
import kunga.rpgcamera.model.PlayerHead;
import kunga.rpgcamera.util.ClientUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.GlfwUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public final class MinecraftClientMixin {
    @Unique
    MinecraftClient self = (MinecraftClient) (Object) this;

    @Unique
    private double lastRenderTime = Double.MIN_VALUE;

    @Inject(method = "render", at = @At("HEAD"))
    private void rpg$render(boolean tick, CallbackInfo ci) {
        if (!ClientUtil.isIngame(self) || !self.isWindowFocused() || !ClientUtil.isRpgThirdPerson(self)) {
            lastRenderTime = GlfwUtil.getTime();
            return;
        }

        if (self.mouse.isCursorLocked()) {
            self.mouse.unlockCursor();
        }

        var now = GlfwUtil.getTime();
        var deltaTime = (lastRenderTime == Double.MIN_VALUE) ? 0.0 : (now - lastRenderTime);
        lastRenderTime = now;

        if (deltaTime <= 0.0 || deltaTime > 0.5)
            return;

        PlayerHead.update(deltaTime);
        RpgMouseInput.update(self.player, deltaTime);

        var turnSpeedInDegreePerSecond = RpgPlayerInput.getTurnSpeedInDegreesPerSecond();
        if (turnSpeedInDegreePerSecond == 0)
            return;

        var yawDelta = (float) (turnSpeedInDegreePerSecond * deltaTime);

        self.player.setYaw(self.player.getYaw() + yawDelta);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tutorialmod$tick(CallbackInfo ci) {
        // RightClickController.update();
        // if (RightClickController.isJustReleased()) {
        // boolean dragged = OrbitCameraController.consumeMovedFlagOnRelease();
        // if (!dragged) {
        // if (self.currentScreen == null && self.player != null
        // && !self.player.isUsingItem()) {
        // self.doItemUse();
        // }
        // }
        // }
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;isCursorLocked()Z"))
    private boolean rpg$handleInputEvents_isCursorLocked(Mouse mouse) {
        return true;
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;doItemUse()V", ordinal = 0))
    private void rpg$handleInputEvents_doItemOnWasPressed(MinecraftClient self) {
        // Do nothing.
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;doItemUse()V", ordinal = 1))
    private void rpg$handleInputEvents_doItemOnIsPressed(MinecraftClient self) {
        // Do nothing.
    }
}
