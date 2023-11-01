package cn.ussshenzhou.mobs.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author USS_Shenzhou
 */
@Mixin(NearestAttackableTargetGoal.class)
public interface NearestAttackableTargetGoalAccessor<T extends LivingEntity> {
    @Accessor
    TargetingConditions getTargetConditions();

    @Accessor
    Class<T> getTargetType();
}
