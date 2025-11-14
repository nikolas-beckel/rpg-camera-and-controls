package kunga.rpgcamera.mixin.client.mouse;

import kunga.rpgcamera.input.Keybinds;
import kunga.rpgcamera.input.RpgMouseInput;
import kunga.rpgcamera.util.ClientUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public final class MouseMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void rpg$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client == null) return;

        if (!ClientUtil.isIngame(client) || !ClientUtil.isRpgThirdPerson(client) || !Keybinds.TOGGLE_ZOOM_KEY.isPressed()) {
            return;
        }

        RpgMouseInput.nudgeByScroll(client.player, vertical);
        ci.cancel();
    }

}
