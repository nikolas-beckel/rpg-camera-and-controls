package kunga.rpgcameraandcontrols.network;

import kunga.rpgcameraandcontrols.RpgCameraAndControls;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TargetC2SPayload(int targetId) implements CustomPayload {
    public static final Identifier TARGET_PAYLOAD_ID = Identifier.of(RpgCameraAndControls.MOD_ID, "target");
    public static final Id<TargetC2SPayload> ID = new Id<>(TARGET_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, TargetC2SPayload> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, TargetC2SPayload::targetId, TargetC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
