package cn.ussshenzhou.mobs.mixin;

import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * @author USS_Shenzhou
 */
@Mixin(TargetGoal.class)
public class TargetGoalMixin {

    @ModifyVariable(method = "<init>(Lnet/minecraft/world/entity/Mob;ZZ)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static boolean lockMustSeeToFalse(boolean value) {
        return false;
    }
}
