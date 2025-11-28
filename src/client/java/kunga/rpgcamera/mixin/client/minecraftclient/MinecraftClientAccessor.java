package kunga.rpgcamera.mixin.client.minecraftclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.tutorial.TutorialManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {

    @Accessor("tutorialManager")
    TutorialManager tutorialManager();

}
