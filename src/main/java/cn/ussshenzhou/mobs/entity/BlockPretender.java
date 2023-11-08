package cn.ussshenzhou.mobs.entity;

import cn.ussshenzhou.mobs.Mobs;
import cn.ussshenzhou.mobs.ai.PriorityAttackHoldingLightSourceTargetGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * @author USS_Shenzhou
 */
public class BlockPretender extends PathfinderMob {
    public static final EntityDataAccessor<Boolean> PRETENDING = SynchedEntityData.defineId(BlockPretender.class, EntityDataSerializers.BOOLEAN);
    private final HashMap<Integer, Goal> TARGET_GOALS = new HashMap<>();

    public BlockPretender(EntityType<BlockPretender> pEntityType, Level pLevel) {
        super(Mobs.BLOCK_PRETENDER_ENTITY_TYPE.get(), pLevel);
        TARGET_GOALS.put(-3, new HurtByTargetGoal(this));
        TARGET_GOALS.put(-2, new PriorityAttackHoldingLightSourceTargetGoal(this));
        TARGET_GOALS.put(-1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.ARMOR, 6)
                .add(Attributes.MAX_HEALTH, 20);
    }

    public static boolean canSpawn(EntityType<BlockPretender> pType, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
        return pLevel.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(-3, new MeleeAttackGoal(this, 1, true));
        this.goalSelector.addGoal(-1, new MoveTowardsTargetGoal(this, 0.8, 32));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (tickCount % 10 == 0 && random.nextFloat() < 0.2) {
                var player = level().getNearestPlayer(TargetingConditions.DEFAULT, this, this.getX(), this.getEyeY(), this.getZ());
                if (player != null && this.getEyePosition().distanceToSqr(player.position()) <= 9) {
                    if (getEntityData().get(PRETENDING)) {
                        this.activate();
                    }
                }
            }
            if (activating) {
                if (getTarget() != null) {
                    var pos = this.blockPosition().below();
                    var state = level().getBlockState(pos);
                    this.playSound(state.getSoundType().getBreakSound(), 1, 1);
                    level().levelEvent(2001, this.blockPosition(), Block.getId(state));
                    getEntityData().set(PRETENDING, false);
                    activating = false;
                }
            }
        }
    }

    private boolean activating = false;

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        var r = super.hurt(pSource, pAmount);
        if (r) {
            if (getEntityData().get(PRETENDING)) {
                this.activate();
            }
        }
        return r;
    }

    private void activate() {
        TARGET_GOALS.forEach(this.targetSelector::addGoal);
        activating = true;
    }

    @Override
    protected void playStepSound(BlockPos pPos, BlockState pState) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.2F);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        var r = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
        var pos = this.position().toVector3f().floor().add(0.5f, 0.5f, 0.5f);
        this.setPos(pos.x, pos.y, pos.z);
        return r;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(PRETENDING, true);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
}
