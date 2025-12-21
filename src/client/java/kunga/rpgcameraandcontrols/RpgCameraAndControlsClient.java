
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

        // Buffer für Linie erstellen
        buffer = new BufferBuilder(allocator, LINE_THROUGH_WALLS.getVertexFormatMode(), LINE_THROUGH_WALLS.getVertexFormat());

        // Linie zeichnen: Start (rot) -> Ende (gelb)
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        buffer.vertex(matrix, (float) savedRayStart.x, (float) savedRayStart.y, (float) savedRayStart.z)
            .color(1.0f, 0.0f, 0.0f, 1.0f); // Rot am Start

        buffer.vertex(matrix, (float) savedRayEnd.x, (float) savedRayEnd.y, (float) savedRayEnd.z)
            .color(1.0f, 1.0f, 0.0f, 1.0f); // Gelb am Ende

        matrices.pop();

        // Buffer zum GPU hochladen und rendern
        drawLineThroughWalls(client, LINE_THROUGH_WALLS);
    }

    private void drawLineThroughWalls(MinecraftClient client, RenderPipeline pipeline) {
        if (buffer == null) return;

        BuiltBuffer builtBuffer = buffer.end();
        BuiltBuffer.DrawParameters drawParameters = builtBuffer.getDrawParameters();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = upload(drawParameters, format, builtBuffer);
        draw(client, pipeline, builtBuffer, drawParameters, vertices, format);

        if (vertexBuffer != null) {
            vertexBuffer.rotate();
        }
        buffer = null;
    }

    private GpuBuffer upload(BuiltBuffer.DrawParameters drawParameters, VertexFormat format, BuiltBuffer builtBuffer) {
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }
            vertexBuffer = new MappableRingBuffer(
                () -> RpgCameraAndControls.MOD_ID + " raycast debug line",
                GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
                Math.max(vertexBufferSize, 256) // Mindestgröße
            );
        }

        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(
            vertexBuffer.getBlocking().slice(0, builtBuffer.getBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.getBuffer(), mappedView.data());
        }

        return vertexBuffer.getBlocking();
    }

    private void draw(MinecraftClient client, RenderPipeline pipeline, BuiltBuffer builtBuffer,
                      BuiltBuffer.DrawParameters drawParameters, GpuBuffer vertices, VertexFormat format) {

        RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
        GpuBuffer indices = shapeIndexBuffer.getIndexBuffer(drawParameters.indexCount());
        VertexFormat.IndexType indexType = shapeIndexBuffer.getIndexType();

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
            .write(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, RenderSystem.getModelOffset(),
                RenderSystem.getTextureMatrix(), 1f);

        try (RenderPass renderPass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(
                () -> RpgCameraAndControls.MOD_ID + " raycast debug rendering",
                client.getFramebuffer().getColorAttachmentView(),
                OptionalInt.empty(),
                client.getFramebuffer().getDepthAttachmentView(),
                OptionalDouble.empty())) {

            renderPass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);
            renderPass.drawIndexed(0, 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }

    public void close() {
        allocator.close();
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }
}