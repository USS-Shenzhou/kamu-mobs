package cn.ussshenzhou.mobs.mixin;

import cn.ussshenzhou.mobs.MobsConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author USS_Shenzhou
 */
@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @ModifyConstant(method = "getSkyDarken", constant = @Constant(floatValue = 0.2f))
    private float darker(float constant) {
        return ConfigHelper.getConfigRead(MobsConfig.class).nightDarkness;
    }
}
