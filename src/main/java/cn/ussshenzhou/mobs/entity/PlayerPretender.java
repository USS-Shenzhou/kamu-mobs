package cn.ussshenzhou.mobs.entity;

import cn.ussshenzhou.mobs.Mobs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
public class PlayerPretender extends PathfinderMob {
    public static final EntityDataAccessor<Boolean> PRETENDING = SynchedEntityData.defineId(PlayerPretender.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Optional<UUID>> PRETENDED_PLAYER = SynchedEntityData.defineId(PlayerPretender.class, EntityDataSerializers.OPTIONAL_UUID);
    protected Vec3 deltaMovementOnPreviousTick = Vec3.ZERO;
    public static final UUID BACKUP_UUID = UUID.fromString("055ae906-5e48-4077-8470-6254c15077b7");

    public PlayerPretender(EntityType<PlayerPretender> pEntityType, Level pLevel) {
        super(Mobs.PLAYER_PRETENDER_ENTITY_TYPE.get(), pLevel);
    }


    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        @SuppressWarnings("OverrideOnly") var r = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
        var pretendedPlayer = pLevel.players().stream()
                .map(Entity::getUUID)
                .skip(pLevel.getLevel().random.nextInt(pLevel.players().size()))
                .findFirst()
                .orElse(BACKUP_UUID);
        this.getEntityData().set(PRETENDED_PLAYER, Optional.of(pretendedPlayer));
        return r;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.ARMOR, 6)
                .add(Attributes.MAX_HEALTH, 20);
    }

    @Override
    public void tick() {
        super.tick();
        this.deltaMovementOnPreviousTick = this.getDeltaMovement();
    }

    public static boolean canSpawn(EntityType<PlayerPretender> pType, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
        return pLevel.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(PRETENDING, true);
        this.getEntityData().define(PRETENDED_PLAYER, Optional.of(BACKUP_UUID));
    }

    @OnlyIn(Dist.CLIENT)
    ResourceLocation skin = null;

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getSkinTextureLocation() {
        if (!isPretending()) {
            return new ResourceLocation(Mobs.MODID, "textures/entity/player_pretender.png");
        }
        if (skin == null) {
            if (pretendedPlayer().isEmpty()) {
                return new ResourceLocation("empty");
            }
            var p = pretendedPlayer().get();
            skin = ((AbstractClientPlayer) p).getSkinTextureLocation();
        }
        return skin;
    }

    public Optional<Player> pretendedPlayer() {
        return Optional.ofNullable(level().getPlayerByUUID(getEntityData().get(PRETENDED_PLAYER).orElse(BACKUP_UUID)));
    }

    public Scoreboard getScoreboard() {
        return level().getScoreboard();
    }

    @Override
    public String getScoreboardName() {
        return pretendedPlayer().map(Player::getScoreboardName).orElse("");
    }

    @Override
    public float getNameTagOffsetY() {
        return pretendedPlayer().map(Player::getNameTagOffsetY).orElse(2f);
    }

    @Override
    public Component getDisplayName() {
        return pretendedPlayer().map(Player::getDisplayName).orElse(Component.empty());
    }

    public boolean isPretending() {
        return getEntityData().get(PRETENDING);
    }

    public Vec3 getDeltaMovementLerped(float pPartialTick) {
        return this.deltaMovementOnPreviousTick.lerp(this.getDeltaMovement(), pPartialTick);
    }
}
