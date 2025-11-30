package kunga.rpgcamera.mixin.client.minecraftclient;

import kunga.rpgcamera.RPGCamera;
import kunga.rpgcamera.camera.RpgCamera;
import kunga.rpgcamera.input.Keybinds;
import kunga.rpgcamera.input.RpgMouseInput;
import kunga.rpgcamera.input.RpgPlayerInput;
import kunga.rpgcamera.input.UseKeyInput;
import kunga.rpgcamera.model.PlayerHead;
import kunga.rpgcamera.util.ClientUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.GlfwUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public final class MinecraftClientMixin {
    @Shadow
    @Final
    public GameOptions options;
    @Shadow
    @Nullable
    public ClientPlayerEntity player;
    @Unique
    MinecraftClient self = (MinecraftClient) (Object) this;
    @Unique
    MinecraftClientAccessor accessor;

    @Unique
    private double lastRenderTime = Double.MIN_VALUE;
    @Unique
    private boolean wasInRpgPerspective = false;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initialization(RunArgs args, CallbackInfo ci) {
        this.accessor = (MinecraftClientAccessor) self;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void rpg$render(boolean tick, CallbackInfo ci) {
        boolean isInRpgMode = ClientUtil.isIngame(self) && ClientUtil.isRpgThirdPerson(self);
        if (wasInRpgPerspective && !isInRpgMode) {
            if (self.currentScreen == null) {
                self.mouse.lockCursor();
            }
        }
        wasInRpgPerspective = isInRpgMode;

        if (self.player == null || !ClientUtil.isIngame(self) || !self.isWindowFocused() || !ClientUtil.isRpgThirdPerson(self)) {
            lastRenderTime = GlfwUtil.getTime();
            return;
        }

        if (self.mouse.isCursorLocked()) {
            self.mouse.unlockCursor();
        }

        var now = GlfwUtil.getTime();
        var deltaTime = (lastRenderTime == Double.MIN_VALUE) ? 0.0 : (now - lastRenderTime);
        lastRenderTime = now;

        if (deltaTime <= 0.0 || deltaTime > 0.5)
            return;

        PlayerHead.update(deltaTime);
        RpgCamera.applyScrollZoom(self.player);
        RpgCamera.updateZoom(self.player, deltaTime);

        var turnSpeedInDegreePerSecond = RpgPlayerInput.getTurnSpeedInDegreesPerSecond();
        if (turnSpeedInDegreePerSecond == 0)
            return;

        var yawDelta = (float) (turnSpeedInDegreePerSecond * deltaTime);

        self.player.setYaw(self.player.getYaw() + yawDelta);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void rpg$tick(CallbackInfo ci) {
        if (!ClientUtil.isRpgThirdPerson(self)) {
            return;
        }

        UseKeyInput.tick(self);

        if (UseKeyInput.isJustReleased() && self.options.useKey.getBoundKeyTranslationKey().equals("key.mouse.right")) {
            boolean dragged = RpgCamera.consumeMovedFlagOnRelease();
            if (!dragged) {
                if (self.currentScreen == null && self.player != null && !self.player.isUsingItem()) {
                    this.accessor.invokeDoItemUse();
                }
            }
        }
    }

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void rpg$handleInputEvents(CallbackInfo ci) {
        if (!ClientUtil.isRpgThirdPerson(self) || self.player == null) {
            return;
        }

        while (self.options.dropKey.wasPressed()) {
            // Remove pressed counts for vanilla code.
        }
        while (Keybinds.DROP_ITEM.wasPressed()) {
            if (!self.player.isSpectator()) {
                self.player.dropSelectedItem(Screen.hasControlDown());
            }
        }

        while (self.options.inventoryKey.wasPressed()) {
            // Remove pressed counts for vanilla code.
        }
        while (Keybinds.OPEN_INVENTORY.wasPressed()) {
            if (self.interactionManager.hasRidingInventory()) {
                this.player.openRidingInventory();
            } else {
                this.accessor.tutorialManager().onInventoryOpened();
                self.setScreen(new InventoryScreen(self.player));
            }
        }
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;isCursorLocked()Z"))
    private boolean rpg$handleInputEvents_isCursorLocked(Mouse mouse) {
        if (ClientUtil.isRpgThirdPerson(self)) {
            return true;
        }

        return self.mouse.isCursorLocked();
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;doItemUse()V", ordinal = 0))
    private void rpg$handleInputEvents_doItemOnWasPressed(MinecraftClient self) {
        if (ClientUtil.isRpgThirdPerson(self) && self.options.useKey.getBoundKeyTranslationKey().equals("key.mouse.right")) {
            return;
        }
        this.accessor.invokeDoItemUse();
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;doItemUse()V", ordinal = 1))
    private void rpg$handleInputEvents_doItemOnIsPressed(MinecraftClient self) {
        if (ClientUtil.isRpgThirdPerson(self) && self.options.useKey.getBoundKeyTranslationKey().equals("key.mouse.right")) {
            return;
        }
        this.accessor.invokeDoItemUse();
    }
}
