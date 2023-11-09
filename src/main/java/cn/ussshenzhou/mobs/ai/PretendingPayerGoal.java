package cn.ussshenzhou.mobs.ai;

import cn.ussshenzhou.mobs.entity.PlayerPretender;
import net.minecraft.world.entity.ai.goal.Goal;
/**
 * @author USS_Shenzhou
 */
public class PretendingPayerGoal extends Goal {
    private final PlayerPretender mob;

    public PretendingPayerGoal(PlayerPretender mob) {
        this.mob = mob;

    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void tick() {

    }
}
