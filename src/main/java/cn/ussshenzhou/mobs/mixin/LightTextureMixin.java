package cn.ussshenzhou.mobs.mixin;

import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author USS_Shenzhou
 */
@Mixin(LightTexture.class)
public class LightTextureMixin {

    @ModifyConstant(method = "updateLightTexture",constant = @Constant(floatValue = 0.95f))
    private float darkerNight1(float constant){
        return 1;
    }

    @ModifyConstant(method = "updateLightTexture",constant = @Constant(floatValue = 0.05f))
    private float darkerNight2(float constant){
        return 0;
    }

    @ModifyConstant(method = "updateLightTexture",constant = @Constant(floatValue = 0.75f))
    private float darkerNight3(float constant){
        return 1;
    }

    @ModifyConstant(method = "updateLightTexture",constant = @Constant(floatValue = 0.04f))
    private float darkerNight4(float constant){
        return 0;
    }
}
