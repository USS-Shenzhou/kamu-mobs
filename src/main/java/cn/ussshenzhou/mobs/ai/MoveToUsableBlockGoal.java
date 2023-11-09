package cn.ussshenzhou.mobs.ai;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import static net.minecraft.world.level.block.Blocks.*;

/**
 * @author USS_Shenzhou
 */
public class MoveToUsableBlockGoal extends MoveToBlockGoal {
    private static final HashSet<Block> USABLE_BLOCK = new HashSet<>() {{
        addAll(List.of(FURNACE, BLAST_FURNACE, SMOKER, CRAFTING_TABLE));
    }};

    public MoveToUsableBlockGoal(PathfinderMob pMob) {
        super(pMob, 1, 16, 4);
        setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }

    private boolean swung = false;

    @Override
    public void start() {
        super.start();
        swung = false;
    }

    @Override
    protected void moveMobToBlock() {
        this.mob.getNavigation().moveTo(this.blockPos.getX() + 0.5, this.blockPos.getY(), this.blockPos.getZ() + 0.5, this.speedModifier);

    }

    @Override
    public double acceptedDistance() {
        return 2;
    }

    @Override
    public void tick() {
        super.tick();
        mob.getLookControl().setLookAt(this.blockPos.getCenter());
        if (isReachedTarget()) {
            mob.getNavigation().stop();
            if (!swung && !this.mob.swinging) {
                this.mob.swing(InteractionHand.MAIN_HAND);
                swung = true;
                this.stop();
            }
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
        return USABLE_BLOCK.contains(pLevel.getBlockState(pPos).getBlock());
    }
}
