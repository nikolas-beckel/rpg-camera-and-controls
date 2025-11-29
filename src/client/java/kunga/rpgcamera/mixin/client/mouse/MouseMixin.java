package kunga.rpgcamera.mixin.client.mouse;

import kunga.rpgcamera.camera.RpgCamera;
import kunga.rpgcamera.input.Keybinds;
import kunga.rpgcamera.input.RpgMouseInput;
import kunga.rpgcamera.util.ClientUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;

@Mixin(Mouse.class)
public final class MouseMixin {

    @Unique
    Mouse self = (Mouse) (Object) this;

    @Unique
    MouseAccessor accessor;

    @Unique
    private double storedCursorX;

    @Unique
    private double storedCursorY;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void rpg$initialization(CallbackInfo ci) {
        this.accessor = (MouseAccessor) self;
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void rpg$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client == null)
            return;

        if (!ClientUtil.isIngame(client) || !ClientUtil.isRpgThirdPerson(client)
                || !Keybinds.TOGGLE_ZOOM_KEY.isPressed()) {
            return;
        }

        RpgMouseInput.addScrollDelta(vertical);
        ci.cancel();
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void rpg$onMouseButton(long window, int button, int action, int modifiers, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client == null)
            return;

        if (!ClientUtil.isIngame(client) || !ClientUtil.isRpgThirdPerson(client)) {
            return;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (action == GLFW.GLFW_PRESS) {
                double[] x = new double[1];
                double[] y = new double[1];
                GLFW.glfwGetCursorPos(window, x, y);
                this.storedCursorX = x[0];
                this.storedCursorY = y[0];

                GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
                RpgMouseInput.onRightMouseDown();
            } else if (action == GLFW.GLFW_RELEASE) {
                RpgMouseInput.onRightMouseUp();
                GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
                GLFW.glfwSetCursorPos(window, this.storedCursorX, this.storedCursorY);
            }
        }
    }

    @Inject(method = "onCursorPos", at = @At("TAIL"))
    private void rpg$onCursorPos(long window, double x, double y, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client == null || window != client.getWindow().getHandle() || client.currentScreen != null) {
            return;
        }

        RpgMouseInput.updateCursorDelta(this.accessor.getCursorDeltaX(), this.accessor.getCursorDeltaY());
        RpgCamera.updateOrbitFromMouseDelta();
    }

    @Inject(method = "lockCursor", at = @At("HEAD"), cancellable = true)
    private void rpg$lockCursor(CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client == null || !ClientUtil.isIngame(client) || !ClientUtil.isRpgThirdPerson(client))
            return;

        this.accessor.setCursorLocked(false);
        ci.cancel();
    }
}
