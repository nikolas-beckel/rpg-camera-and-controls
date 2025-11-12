package kunga.rpgcamera.mixin.client.keyboardinput;

import kunga.rpgcamera.input.Keybinds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public final class KeyboardInputMixin {
    @Unique
    KeyboardInput self = (KeyboardInput) (Object) this;

    @Unique
    KeyboardInputAccessor accessor;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initialization(CallbackInfo ci) {
        this.accessor = (KeyboardInputAccessor) self;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void rpgcamera$tick(CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        var options = client.options;
        if (options.getPerspective() != Perspective.THIRD_PERSON_BACK) return;

        ci.cancel();

        boolean strafeLeft = Keybinds.STRAFE_LEFT_KEY.isPressed();
        boolean strafeRight = Keybinds.STRAFE_RIGHT_KEY.isPressed();
        boolean turnLeft = Keybinds.TURN_LEFT_KEY.isPressed();
        boolean turnRight = Keybinds.TURN_RIGHT_KEY.isPressed();

        var playerInput = new PlayerInput(
            options.forwardKey.isPressed(),
            options.backKey.isPressed(),
            strafeLeft,
            strafeRight,
            options.jumpKey.isPressed(),
            options.sneakKey.isPressed(),
            options.sprintKey.isPressed()
        );
        accessor.setPlayerInput(playerInput);

        float sideways = accessor.invokeGetMovementMultiplier(strafeLeft, strafeRight);
        float forward = accessor.invokeGetMovementMultiplier(playerInput.forward(), playerInput.backward());
        Vec2f movementVector = (forward == 0.0f && sideways == 0.0f)
            ? Vec2f.ZERO
            : new Vec2f(sideways, forward).normalize();
        accessor.setMovementVector(movementVector);

        int turnDirection = (turnRight ? 1 : 0) + (turnLeft ? -1 : 0);
        double turnSpeed = 180;

        if (playerInput.sprint()) turnSpeed *= 1.20;
        if (playerInput.sneak()) turnSpeed *= 0.60;
    }
}
