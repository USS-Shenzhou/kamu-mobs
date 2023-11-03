package cn.ussshenzhou.mobs.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * @author USS_Shenzhou
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @ModifyConstant(method = "levelEvent",
            slice = @Slice(
                    from = @At(value = "FIELD",target = "Lnet/minecraft/sounds/SoundEvents;ZOMBIE_ATTACK_WOODEN_DOOR:Lnet/minecraft/sounds/SoundEvent;"),
                    to = @At(value = "FIELD",target = "Lnet/minecraft/sounds/SoundEvents;ZOMBIE_ATTACK_IRON_DOOR:Lnet/minecraft/sounds/SoundEvent;")
            ),
            constant = @Constant(floatValue = 2.0f)
    )
    private float lowerVolume(float constant) {
        return 0.075f;
    }

    @ModifyConstant(method = "levelEvent",
            slice = @Slice(
                    from = @At(value = "FIELD",target = "Lnet/minecraft/sounds/SoundEvents;ZOMBIE_ATTACK_WOODEN_DOOR:Lnet/minecraft/sounds/SoundEvent;"),
                    to = @At(value = "FIELD",target = "Lnet/minecraft/sounds/SoundEvents;ZOMBIE_ATTACK_IRON_DOOR:Lnet/minecraft/sounds/SoundEvent;")
            ),
            constant = @Constant(floatValue = 0.2f)
    )
    private float lowerPitch0(float constant) {
        return 0.15f;
    }

    @ModifyConstant(method = "levelEvent",
            slice = @Slice(
                    from = @At(value = "FIELD",target = "Lnet/minecraft/sounds/SoundEvents;ZOMBIE_ATTACK_WOODEN_DOOR:Lnet/minecraft/sounds/SoundEvent;"),
                    to = @At(value = "FIELD",target = "Lnet/minecraft/sounds/SoundEvents;ZOMBIE_ATTACK_IRON_DOOR:Lnet/minecraft/sounds/SoundEvent;")
            ),
            constant = @Constant(floatValue = 1.0f)
    )
    private float lowerPitch1(float constant) {
        return 0.25f;
    }
}
