package kunga.rpgcamera.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;

public final class ClientUtil {
    public static boolean isIngame(MinecraftClient client) {
        return client.currentScreen == null && client.player != null && !client.player.isSpectator();
    }

    public static boolean isRpgThirdPerson(MinecraftClient client) {
        return client.options.getPerspective() == Perspective.THIRD_PERSON_BACK;
    }

    public static boolean isLocalPlayer(ClientPlayerEntity player, BipedEntityRenderState state) {
        return (state instanceof PlayerEntityRenderState p) && p.id == player.getId();
    }
}
