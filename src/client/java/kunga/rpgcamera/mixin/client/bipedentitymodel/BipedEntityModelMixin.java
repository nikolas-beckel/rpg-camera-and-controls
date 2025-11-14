package kunga.rpgcamera.mixin.client.bipedentitymodel;

import kunga.rpgcamera.model.PlayerHead;
import kunga.rpgcamera.util.ClientUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public final class BipedEntityModelMixin {
    @Unique
    BipedEntityModel self = (BipedEntityModel) (Object) this;

    @Inject(method = "setAngles", at = @At("TAIL"))
    private void rpg$turnHeadToTurnDirection(BipedEntityRenderState state, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        boolean ingame = client.currentScreen == null && !client.player.isSpectator();
        if (!ingame || !ClientUtil.isRpgThirdPerson(client) || !ClientUtil.isLocalPlayer(client.player, state)) return;

        self.head.pitch = 0;
        self.head.roll = 0;
        self.head.yaw = PlayerHead.getCurrentYawRadiant();
    }
}
