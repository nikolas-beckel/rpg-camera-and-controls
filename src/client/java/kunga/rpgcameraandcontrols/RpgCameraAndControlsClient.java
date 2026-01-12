
package kunga.rpgcameraandcontrols;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import kunga.rpgcameraandcontrols.input.Keybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class RpgCameraAndControlsClient implements ClientModInitializer {

    // Pipeline für Linien die durch Wände sichtbar sind
    private static final RenderPipeline LINE_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
        .withLocation(Identifier.of(RpgCameraAndControls.MOD_ID, "pipeline/debug_line_through_walls"))
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .build()
    );

    private static final BufferAllocator allocator = new BufferAllocator(1024);
    private BufferBuilder buffer;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private MappableRingBuffer vertexBuffer;

    // Gespeicherte Raycast-Linie (Start und Ende in Weltkoordinaten)
    private static Vec3d savedRayStart = null;
    private static Vec3d savedRayEnd = null;

    // Öffentliche Methode zum Setzen der Debug-Linie (wird vom MouseMixin aufgerufen)
    public static void setDebugRaycast(Vec3d start, Vec3d end) {
        savedRayStart = start;
        savedRayEnd = end;
    }

    // Linie löschen
    public static void clearDebugRaycast() {
        savedRayStart = null;
        savedRayEnd = null;
    }

    @Override
    public void onInitializeClient() {
        Keybinds.register();

        // Registriere das World-Rendering Event
        WorldRenderEvents.LAST.register(this::renderRaycastLine);
    }

    private void renderRaycastLine(WorldRenderContext context) {
        // Nur rendern wenn eine Linie gesetzt wurde
        if (savedRayStart == null || savedRayEnd == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        Vec3d camPos = context.camera().getPos();

        MatrixStack matrices = context.matrixStack();
        if (matrices == null) return;

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        RenderLayer lineLayer = RenderLayer.getLines();

        // Buffer passend zum Layer erstellen
        var lineBufferBuilder = new BufferBuilder(allocator, lineLayer.getDrawMode(), lineLayer.getVertexFormat());

        // Linie zeichnen: Start (rot) -> Ende (gelb)
        Matrix4f positionMatrix  = matrices.peek().getPositionMatrix();

        boolean vertexFormatNeedsNormal = lineLayer.getVertexFormat().equals(VertexFormats.POSITION_COLOR_NORMAL);

        if (vertexFormatNeedsNormal) {
            // Start (rot)
            lineBufferBuilder
                .vertex(positionMatrix, (float) savedRayStart.x, (float) savedRayStart.y, (float) savedRayStart.z)
                .color(1.0f, 0.0f, 0.0f, 1.0f)
                .normal(0.0f, 1.0f, 0.0f);

            // Ende (gelb)
            lineBufferBuilder
                .vertex(positionMatrix, (float) savedRayEnd.x, (float) savedRayEnd.y, (float) savedRayEnd.z)
                .color(1.0f, 1.0f, 0.0f, 1.0f)
                .normal(0.0f, 1.0f, 0.0f);
        } else {
            // Start (rot)
            lineBufferBuilder
                .vertex(positionMatrix, (float) savedRayStart.x, (float) savedRayStart.y, (float) savedRayStart.z)
                .color(1.0f, 0.0f, 0.0f, 1.0f);

            // Ende (gelb)
            lineBufferBuilder
                .vertex(positionMatrix, (float) savedRayEnd.x, (float) savedRayEnd.y, (float) savedRayEnd.z)
                .color(1.0f, 1.0f, 0.0f, 1.0f);
        }

        matrices.pop();

        // Buffer finalisieren und direkt über den RenderLayer zeichnen
        try (BuiltBuffer builtBuffer = lineBufferBuilder.end()) {
            lineLayer.draw(builtBuffer);
        }
    }
}