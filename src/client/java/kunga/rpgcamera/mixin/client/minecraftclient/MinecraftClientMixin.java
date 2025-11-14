package kunga.rpgcamera.mixin.client.minecraftclient;

import kunga.rpgcamera.input.RpgPlayerInput;
import kunga.rpgcamera.model.PlayerHead;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.GlfwUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public final class MinecraftClientMixin {
    @Unique
    MinecraftClient self = (MinecraftClient) (Object) this;

    @Unique
    private double lastRenderTime = Double.MIN_VALUE;

    @Inject(method = "render", at = @At("HEAD"))
    private void rpg$render(boolean tick, CallbackInfo ci) {
        var player = self.player;
        if (player == null) return;

        if (self.currentScreen != null || !self.isWindowFocused()) {
            lastRenderTime = GlfwUtil.getTime();
            return;
        }

        var now = GlfwUtil.getTime();
        var deltaTime = (lastRenderTime == Double.MIN_VALUE) ? 0.0 : (now - lastRenderTime);
        lastRenderTime = now;

        if (deltaTime <= 0.0 || deltaTime > 0.5) return;

        PlayerHead.update(deltaTime);
        
        var turnSpeedInDegreePerSecond = RpgPlayerInput.getTurnSpeedInDegreesPerSecond();
        if (turnSpeedInDegreePerSecond == 0) return;

        var yawDelta = (float) (turnSpeedInDegreePerSecond * deltaTime);

        player.setYaw(player.getYaw() + yawDelta);
    }

}
