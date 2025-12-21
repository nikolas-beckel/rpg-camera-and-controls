package kunga.rpgcameraandcontrols.mixin.client.worldrenderer;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    // Injection am Ende der Render-Methode
    // Signatur für 1.21 angepasst (RenderTickCounter statt float, kein MatrixStack Parameter)
    @Inject(method = "render", at = @At("TAIL"))
    private void renderTargetRing(
        ObjectAllocator allocator,
        RenderTickCounter tickCounter,
        boolean renderBlockOutline,
        Camera camera,
        Matrix4f positionMatrix,
        Matrix4f projectionMatrix,
        GpuBufferSlice fog,
        Vector4f fogColor,
        boolean shouldRenderSky,
        CallbackInfo ci
    ) {

    }
}