package kunga.rpgcamera.mixin.client.keyboardinput;

import kunga.rpgcamera.input.Keybinds;
import kunga.rpgcamera.input.RpgPlayerInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.Perspective;
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
    private void rpg$initialization(CallbackInfo ci) {
        this.accessor = (KeyboardInputAccessor) self;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void rpg$tick(CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        var options = client.options;
        if (options.getPerspective() != Perspective.THIRD_PERSON_BACK) return;

        ci.cancel();

        RpgPlayerInput.setMovement(
            options.forwardKey.isPressed(),
            options.backKey.isPressed(),
            Keybinds.STRAFE_LEFT_KEY.isPressed(),
            Keybinds.turnLeftKeyIsPressed(client),
            Keybinds.STRAFE_RIGHT_KEY.isPressed(),
            Keybinds.turnRightKeyIsPressed(client),
            options.jumpKey.isPressed(),
            options.sneakKey.isPressed(),
            options.sprintKey.isPressed()
        );

        accessor.setPlayerInput(RpgPlayerInput.getPlayerInput());
        accessor.setMovementVector(RpgPlayerInput.getMovementVector());
    }
}
