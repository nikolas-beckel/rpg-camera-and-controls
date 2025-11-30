package kunga.rpgcameraandcontrols.mixin.client.mouse;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mouse.class)
public interface MouseAccessor {

    @Accessor("cursorDeltaX")
    double getCursorDeltaX();

    @Accessor("cursorDeltaY")
    double getCursorDeltaY();

    @Accessor("cursorLocked")
    void setCursorLocked(boolean locked);

}
