package kunga.rpgcamera.mixin.client.mouse;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mouse.class)
public interface MouseAccessor {

    @Accessor("cursorLocked")
    void setCursorLocked(boolean locked);

}
