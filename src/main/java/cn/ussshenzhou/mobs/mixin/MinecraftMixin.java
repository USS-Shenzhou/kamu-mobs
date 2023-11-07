package cn.ussshenzhou.mobs.mixin;

import cn.ussshenzhou.mobs.ClientForgeBusListener;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.vehicle.Minecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author USS_Shenzhou
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "resizeDisplay", at = @At("RETURN"))
    private void onResize(CallbackInfo ci) {
        ClientForgeBusListener.resize();
    }
}
