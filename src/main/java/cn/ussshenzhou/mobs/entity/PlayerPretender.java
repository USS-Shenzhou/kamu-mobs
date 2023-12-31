package cn.ussshenzhou.mobs.entity;

import cn.ussshenzhou.mobs.Mobs;
import cn.ussshenzhou.mobs.ai.MoveToUsableBlockGoal;
import cn.ussshenzhou.mobs.ai.PriorityAttackHoldingLightSourceTargetGoal;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    private final MeleeAttackGoal meleeAttackGoal = new MeleeAttackGoal(this, 1, true);
    //private final MoveTowardsTargetGoal moveTowardsTargetGoal = new MoveTowardsTargetGoal(this, 1, 32);
    private final MoveToUsableBlockGoal moveToUsableBlockGoal = new MoveToUsableBlockGoal(this);
    private final OpenDoorGoal openDoorGoal = new OpenDoorGoal(this, false);
    private final RandomLookAroundGoal randomLookAroundGoal = new RandomLookAroundGoal(this);
    private final WaterAvoidingRandomStrollGoal waterAvoidingRandomStrollGoal = new WaterAvoidingRandomStrollGoal(this, 1);

    private final HurtByTargetGoal hurtByTargetGoal = new HurtByTargetGoal(this);
    private final PriorityAttackHoldingLightSourceTargetGoal priorityAttackHoldingLightSourceTargetGoal = new PriorityAttackHoldingLightSourceTargetGoal(this);
    private final NearestAttackableTargetGoal<?> nearestAttackableTargetGoal = new NearestAttackableTargetGoal<>(this, Player.class, false);

    public PlayerPretender(EntityType<PlayerPretender> pEntityType, Level pLevel) {
        super(Mobs.PLAYER_PRETENDER_ENTITY_TYPE.get(), pLevel);
        this.goalSelector.addGoal(0, meleeAttackGoal);
        this.goalSelector.addGoal(2, moveToUsableBlockGoal);
        this.goalSelector.addGoal(3, openDoorGoal);
        this.goalSelector.addGoal(4, randomLookAroundGoal);
        this.goalSelector.addGoal(5, waterAvoidingRandomStrollGoal);
        this.xpReward = 10;
    }

    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        @SuppressWarnings("OverrideOnly") var r = super.finalizeSpawn(level, pDifficulty, pReason, pSpawnData, pDataTag);
        var pretendedPlayer = level.players().stream()
                .map(Entity::getUUID)
                .skip(level.getLevel().random.nextInt(level.players().size()))
                .findFirst()
                .orElse(BACKUP_UUID);
        this.getEntityData().set(PRETENDED_PLAYER, Optional.of(pretendedPlayer));
        this.populateDefaultEquipmentSlots(level.getRandom(), pDifficulty);
        return r;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ARMOR, 6)
                .add(Attributes.MAX_HEALTH, 20);
    }

    @Override
    public void tick() {
        super.tick();
        this.deltaMovementOnPreviousTick = this.getDeltaMovement();
        if (!level().isClientSide()) {
            if (tickCount % 20 == 0) {
                var player = level().getNearestPlayer(TargetingConditions.DEFAULT, this, this.getX(), this.getEyeY(), this.getZ());
                if (player != null) {
                    var d2 = this.getEyePosition().distanceToSqr(player.position());
                    float threshold = 0;
                    if (d2 <= 4 * 4) {
                        threshold = 0.1f;
                    } else if (d2 <= 16 * 16) {
                        threshold = 0.04f;
                    } else if (d2 <= 32 * 32) {
                        threshold = 0.02f;
                    }
                    if (random.nextFloat() < threshold) {
                        mayActivate();
                    }
                }
            }
            if (activating) {
                if (getTarget() != null) {
                    this.playSound(SoundEvents.ITEM_BREAK, 1, 1);
                    getEntityData().set(PRETENDING, false);
                    addActivateParticle();
                    activating = false;
                }
            }
        }
    }

    private void addActivateParticle() {
        var server = ((ServerLevel) level()).getServer();
        var pos = this.position().add(0, 0.9, 0);
        StringBuilder command = new StringBuilder("/mp");
        var name = pretendedPlayer().map(player -> player.getGameProfile().getName()).orElse("");
        if (name.contains("BadCen")) {
            command.append(" madparticle:badcen");
        } else if (name.contains("Melor_")) {
            command.append(" madparticle:melor");
        } else if (name.contains("Uncle_Red")) {
            command.append(" madparticle:uncle_red");
        } else if (name.contains("Hei_Mao")) {
            command.append(" madparticle:heimao");
        } else {
            command.append(" madparticle:kamuamua");
        }
        command.append(" RANDOM 80 TRUE 75");
        command.append(String.format(" %f %f %f", pos.x, pos.y, pos.z));
        command.append(" 0.4 0.9 0.4 0.0 0.05 0.0 0.15 0.1 0.15 TRUE 1 0 0 0.9 0.9 0.3 0.3 0.0 0.0 0.0 0.0 0 FALSE 0 0 PARTICLE_SHEET_TRANSLUCENT 1.000 1.000 1.000 0 1 1 LINEAR 1 1 LINEAR @a {}");
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command.toString());
    }

    private void mayActivate() {
        if (getEntityData().get(PRETENDING)) {
            this.activate();
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
        this.targetSelector.addGoal(0, hurtByTargetGoal);
        this.targetSelector.addGoal(1, priorityAttackHoldingLightSourceTargetGoal);
        this.targetSelector.addGoal(2, nearestAttackableTargetGoal);
        activating = true;
    }

    public static boolean canSpawn(EntityType<PlayerPretender> pType, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
        return pLevel.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom);
    }

    private Item getRandomArmor(RandomSource r, EquipmentSlot slot) {
        float f = r.nextFloat();
        float netherite = 0.05f;
        float diamond = pretendedPlayer().map(player -> player.getGameProfile().getName()).orElse("").contains("BadCen") ? 0.35f : 0.15f;
        float iron = 0.5f;
        switch (slot) {
            case HEAD:
                if (f < netherite) {
                    return Items.NETHERITE_HELMET;
                } else if (f < diamond) {
                    return Items.DIAMOND_HELMET;
                } else if (f < iron) {
                    return Items.IRON_HELMET;
                } else {
                    return null;
                }
            case CHEST:
                if (f < netherite) {
                    return Items.NETHERITE_CHESTPLATE;
                } else if (f < diamond) {
                    return Items.DIAMOND_CHESTPLATE;
                } else if (f < iron) {
                    return Items.IRON_CHESTPLATE;
                } else {
                    return null;
                }
            case LEGS:
                if (f < netherite) {
                    return Items.NETHERITE_LEGGINGS;
                } else if (f < diamond) {
                    return Items.DIAMOND_LEGGINGS;
                } else if (f < iron) {
                    return Items.IRON_LEGGINGS;
                } else {
                    return null;
                }
            case FEET:
                if (f < netherite) {
                    return Items.NETHERITE_BOOTS;
                } else if (f < diamond) {
                    return Items.DIAMOND_BOOTS;
                } else if (f < iron) {
                    return Items.IRON_BOOTS;
                } else {
                    return null;
                }
            default:
                return null;
        }
    }

    private Item getRandomWeapon(RandomSource r) {
        if (pretendedPlayer().map(player -> player.getGameProfile().getName()).orElse("").contains("Hei_Mao")) {
            if (r.nextFloat() < 0.3f) {
                return Items.DIAMOND_SHOVEL;
            }
        }
        float f = r.nextFloat();
        float netherite = 0.05f;
        float diamond = pretendedPlayer().map(player -> player.getGameProfile().getName()).orElse("").contains("BadCen") ? 0.35f : 0.15f;
        float iron = 0.5f;
        float stone = 0.7f;
        if (f < netherite) {
            return r.nextBoolean() ? Items.NETHERITE_SWORD : Items.NETHERITE_AXE;
        } else if (f < diamond) {
            return r.nextBoolean() ? Items.DIAMOND_SWORD : Items.DIAMOND_AXE;
        } else if (f < iron) {
            return r.nextBoolean() ? Items.IRON_SWORD : Items.IRON_AXE;
        } else if (f < stone) {
            return Items.STONE_AXE;
        } else {
            return null;
        }
    }


    @Override
    protected void populateDefaultEquipmentSlots(RandomSource r, DifficultyInstance ignored) {
        for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            if (r.nextFloat() < 0.6f) {
                continue;
            }
            if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack itemstack = this.getItemBySlot(equipmentslot);
                if (itemstack.isEmpty()) {
                    Item item = getRandomArmor(r, equipmentslot);
                    if (item != null) {
                        this.setItemSlot(equipmentslot, new ItemStack(item));
                    }
                }
            } else if (equipmentslot.getType() == EquipmentSlot.Type.HAND) {
                ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
                if (itemstack.isEmpty()) {
                    Item item = getRandomWeapon(r);
                    if (item != null) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(item));
                    }
                }
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(PRETENDING, true);
        this.getEntityData().define(PRETENDED_PLAYER, Optional.of(BACKUP_UUID));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putUUID("pretended_player", getEntityData().get(PRETENDED_PLAYER).orElse(BACKUP_UUID));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.hasUUID("pretended_player")) {
            getEntityData().set(PRETENDED_PLAYER, Optional.of(pCompound.getUUID("pretended_player")));
        }
    }

    ResourceLocation skin = null;

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getSkinTextureLocation() {
        if (!isPretending()) {
            return new ResourceLocation(Mobs.MODID, "textures/entity/player_pretender.png");
        }
        if (skin == null) {
            if (pretendedPlayer().isEmpty()) {
                return new ResourceLocation(Mobs.MODID, "textures/entity/player_pretender.png");
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
