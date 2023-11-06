package cn.ussshenzhou.mobs.entity;

import cn.ussshenzhou.mobs.Mobs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * @author USS_Shenzhou
 */
public class BlockPretenderEntity extends Mob {
    public static final EntityDataAccessor<Boolean> PRETENDING = SynchedEntityData.defineId(BlockPretenderEntity.class, EntityDataSerializers.BOOLEAN);

    public BlockPretenderEntity(EntityType<BlockPretenderEntity> pEntityType, Level pLevel) {
        super(Mobs.BLOCK_PRETENDER_ENTITY_TYPE.get(), pLevel);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.ARMOR, 6)
                .add(Attributes.MAX_HEALTH, 20);
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
