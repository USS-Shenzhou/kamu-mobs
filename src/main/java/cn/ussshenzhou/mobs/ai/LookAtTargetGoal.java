package cn.ussshenzhou.mobs.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;

import java.util.EnumSet;

/**
 * @author USS_Shenzhou
 */
public class LookAtTargetGoal extends Goal {
    private Mob monster;

    public LookAtTargetGoal(Mob monster) {
        this.monster = monster;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return monster.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return monster.getTarget() != null;
    }

    @Override
    public void tick() {
        var lookAt = monster.getTarget();
        if (lookAt == null) {
            return;
        }
        monster.getLookControl().setLookAt(lookAt.getX(), lookAt.getEyeY(), lookAt.getZ());
    }
}
