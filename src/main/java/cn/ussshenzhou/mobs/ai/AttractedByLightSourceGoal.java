package cn.ussshenzhou.mobs.ai;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.LightLayer;
import org.joml.Vector3f;

import java.util.EnumSet;

/**
 * @author USS_Shenzhou
 */
public class AttractedByLightSourceGoal extends Goal {
    BlockPos brighter = null;
    final PathfinderMob mob;

    public AttractedByLightSourceGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }


    @Override
    public boolean canUse() {
        if (mob.getRandom().nextFloat() < 0.1) {
            return searchForSource();
        }
        return false;
    }

    public static final int RANGE = 8;

    private boolean searchForSource() {
        var p = mob.getOnPos().offset(0, 1, 0);
        int l0 = mob.level().getBrightness(LightLayer.BLOCK, p);
        if (l0 == 0) {
            return false;
        }
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(p.getX() + RANGE, p.getY() + 2, p.getZ() + RANGE);
        blockPos.move(-RANGE * 2, 0, 0);
        for (int x = 0; x < RANGE * 2; x++) {
            blockPos.move(0, -4, 0);
            for (int y = 0; y < 4; y++) {
                blockPos.move(0, 0, -RANGE * 2);
                for (int z = 0; z < RANGE * 2; z++) {
                    var l = mob.level().getLightEngine().blockEngine.getLightValue(blockPos);
                    if (l > l0) {
                        l0 = l;
                        brighter = blockPos.immutable();
                    }
                    blockPos.move(0, 0, 1);
                }
                blockPos.move(0, 1, 0);
            }
            blockPos.move(1, 0, 0);
        }
        return brighter != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(brighter.getX() + 0.5, brighter.getY() + 0.5, brighter.getZ() + 0.5, 1);
    }

    @Override
    public void stop() {
        brighter = null;
    }
}
