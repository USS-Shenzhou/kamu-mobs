package cn.ussshenzhou.mobs.mixin;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * @author USS_Shenzhou
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @ModifyConstant(method = "levelEvent",
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/sounds/SoundEvents;ZOMBIE_ATTACK_WOODEN_DOOR:Lnet/minecraft/sounds/SoundEvent;"),
                    to = @At(value = "FIELD", target = "Lnet/minecraft/sounds/SoundEvents;ZOMBIE_ATTACK_IRON_DOOR:Lnet/minecraft/sounds/SoundEvent;")
            ),
            constant = @Constant(floatValue = 2.0f)
    )
    private float lowerVolume(float constant) {
        return 0.075f;
    }

    @ModifyConstant(method = "levelEvent",
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/sounds/SoundEvents;ZOMBIE_ATTACK_WOODEN_DOOR:Lnet/minecraft/sounds/SoundEvent;"),
                    to = @At(value = "FIELD", target = "Lnet/minecraft/sounds/SoundEvents;ZOMBIE_ATTACK_IRON_DOOR:Lnet/minecraft/sounds/SoundEvent;")
            ),
            constant = @Constant(floatValue = 0.2f)
    )
    private float lowerPitch0(float constant) {
        return 0.15f;
    }

    @ModifyConstant(method = "levelEvent",
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/sounds/SoundEvents;ZOMBIE_ATTACK_WOODEN_DOOR:Lnet/minecraft/sounds/SoundEvent;"),
                    to = @At(value = "FIELD", target = "Lnet/minecraft/sounds/SoundEvents;ZOMBIE_ATTACK_IRON_DOOR:Lnet/minecraft/sounds/SoundEvent;")
            ),
            constant = @Constant(floatValue = 1.0f)
    )
    private float lowerPitch1(float constant) {
        return 0.25f;
    }
}
