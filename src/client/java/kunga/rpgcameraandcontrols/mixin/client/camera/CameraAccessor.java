package kunga.rpgcameraandcontrols.mixin.client.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.render.Camera;

@Mixin(Camera.class)
public interface CameraAccessor {

    @Invoker("setPos")
    void invokeSetPos(double x, double y, double z);

    @Invoker("setRotation")
    void invokeSetRotation(float yaw, float pitch);

}
