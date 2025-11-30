package kunga.rpgcameraandcontrols.mixin.client.handledscreen;

import kunga.rpgcameraandcontrols.input.Keybinds;
import kunga.rpgcameraandcontrols.util.ClientUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void rpg$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        var client = MinecraftClient.getInstance();
        if (!ClientUtil.isRpgThirdPerson(client)) {
            return;
        }

        if (Keybinds.OPEN_INVENTORY.matchesKey(keyCode, scanCode)) {
            this.close();
            cir.setReturnValue(true);
        }
    }
}
