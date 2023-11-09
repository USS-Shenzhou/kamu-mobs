package cn.ussshenzhou.mobs.mixin;

import cn.ussshenzhou.mobs.MobsConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author USS_Shenzhou
 */
@Mixin(MobCategory.class)
public class MobCategoryMixin {

    @Inject(method = "getMaxInstancesPerChunk", at = @At("HEAD"), cancellable = true)
    private void moreMonsters(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this == MobCategory.MONSTER) {
            cir.setReturnValue((int) (70 * ConfigHelper.getConfigRead(MobsConfig.class).mobAmountFactor));
        }
    }
}
