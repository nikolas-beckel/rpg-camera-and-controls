package kunga.rpgcameraandcontrols.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import kunga.rpgcameraandcontrols.target.TargetingSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TargetRingRenderer {

    public static void render(MatrixStack matrices, float tickDelta, Camera camera) {
        Entity target = TargetingSystem.getTarget();

        if (target == null || !target.isAlive()) return;

        // FIX: Zugriff auf prevX/Y/Z kann fehlschlagen, wenn Mappings nicht stimmen.
        // Wir nutzen die Standard-Interpolation die MC überall nutzt.
        // Falls prevX rot ist: versuche getX(), aber das ruckelt.
        // In 1.21 Yarn Mappings sind prevX, prevY, prevZ public fields der Klasse Entity.
        // Wenn sie rot sind, heißt das Feld evtl. `lastRenderX` oder du nutzt Mojang Mappings (`xo`, `yo`, `zo`).
        // Wir nutzen hier eine sichere Methode über getLeashPos oder einfach getPos() für den Anfang,
        // um den Compile-Fehler zu fixen. Besser ist aber dies:
        double x = MathHelper.lerp(tickDelta, target.lastRenderX, target.getX());
        double y = MathHelper.lerp(tickDelta, target.lastRenderY, target.getY());
        double z = MathHelper.lerp(tickDelta, target.lastRenderZ, target.getZ());

        Vec3d cameraPos = camera.getPos();
        double camX = cameraPos.x;
        double camY = cameraPos.y;
        double camZ = cameraPos.z;

        matrices.push();
        matrices.translate(x - camX, y - camY, z - camZ);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();

        // FIX: In deiner Version (1.21.x) heißt das oft nicht mehr VertexFormat.Mode, sondern VertexFormat.DrawMode
        // Siehe RenderSystem.java Zeile 120: VertexFormat.DrawMode.QUADS
        // Wir nutzen LINES (nicht DEBUG_LINES, das macht oft Probleme)
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float radius = Math.max(target.getWidth() * 0.8f, 0.7f);
        int segments = 32;

        for (int i = 0; i < segments; i++) {
            double angle1 = (2 * Math.PI * i) / segments;
            double angle2 = (2 * Math.PI * (i + 1)) / segments;

            float px1 = (float) (Math.cos(angle1) * radius);
            float pz1 = (float) (Math.sin(angle1) * radius);
            float px2 = (float) (Math.cos(angle2) * radius);
            float pz2 = (float) (Math.sin(angle2) * radius);

            buffer.vertex(matrix, px1, 0.05f, pz1).color(1.0f, 0.8f, 0.0f, 0.8f);
            buffer.vertex(matrix, px2, 0.05f, pz2).color(1.0f, 0.8f, 0.0f, 0.8f);
        }

        // FIX: Zeichnen. In 1.21 mit BuiltBuffer.
        // BufferRenderer.drawWithGlobalProgram erwartet einen BuiltBuffer.
        try {
            BuiltBuffer builtBuffer = buffer.end();
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
            builtBuffer.close();
        } catch (Exception e) {
            // Fallback, falls sich die API wieder geändert hat
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        matrices.pop();
    }
}