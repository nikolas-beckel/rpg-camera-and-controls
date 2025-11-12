package kunga.rpgcamera.mixin.client.keyboardinput;

import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyboardInput.class)
public interface KeyboardInputAccessor extends InputAccessor {

    @Invoker("getMovementMultiplier")
    float invokeGetMovementMultiplier(boolean positive, boolean negative);

}
