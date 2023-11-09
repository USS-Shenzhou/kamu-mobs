package cn.ussshenzhou.mobs.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

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
    }

    private boolean swung = false;

    @Override
    public void start() {
        super.start();
        swung = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (isReachedTarget()) {
            if (!swung && !this.mob.swinging) {
                this.mob.swing(this.mob.getUsedItemHand());
                swung = true;
            }
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
        return USABLE_BLOCK.contains(pLevel.getBlockState(pPos).getBlock());
    }
}
