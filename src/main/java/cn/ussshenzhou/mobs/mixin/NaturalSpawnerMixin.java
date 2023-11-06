package cn.ussshenzhou.mobs.mixin;

import cn.ussshenzhou.mobs.Mobs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {

    @Shadow
    private static Optional<MobSpawnSettings.SpawnerData> getRandomSpawnMobAt(ServerLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, MobCategory pCategory, RandomSource pRandom, BlockPos pPos) {
        return Optional.empty();
    }

    @Shadow
    private static boolean isValidSpawnPostitionForType(ServerLevel pLevel, MobCategory pCategory, StructureManager pStructureManager, ChunkGenerator pGenerator, MobSpawnSettings.SpawnerData pData, BlockPos.MutableBlockPos pPos, double pDistance) {
        return false;
    }

    @Shadow
    @Nullable
    private static Mob getMobForSpawn(ServerLevel pLevel, EntityType<?> pEntityType) {
        return null;
    }

    @Shadow
    private static boolean isValidPositionForMob(ServerLevel pLevel, Mob pMob, double pDistance) {
        return false;
    }

    @ModifyConstant(method = "isRightDistanceToPlayerAndSpawnPoint", constant = @Constant(doubleValue = 24.0))
    private static double closerMonsters(double constant) {
        return 16.0;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static void spawnCategoryForPosition(MobCategory pCategory, ServerLevel pLevel, ChunkAccess pChunk, BlockPos pPos, NaturalSpawner.SpawnPredicate pFilter, NaturalSpawner.AfterSpawnCallback pCallback) {
        StructureManager structuremanager = pLevel.structureManager();
        ChunkGenerator chunkgenerator = pLevel.getChunkSource().getGenerator();
        int i = pPos.getY();
        BlockState blockstate = pChunk.getBlockState(pPos);
        if (!blockstate.isRedstoneConductor(pChunk, pPos)) {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            int j = 0;

            for (int k = 0; k < 3; ++k) {
                int l = pPos.getX();
                int i1 = pPos.getZ();
                int j1 = 6;
                MobSpawnSettings.SpawnerData mobspawnsettings$spawnerdata = null;
                SpawnGroupData spawngroupdata = null;
                int k1 = Mth.ceil(pLevel.random.nextFloat() * 4.0F);
                int l1 = 0;

                for (int i2 = 0; i2 < k1; ++i2) {
                    l += pLevel.random.nextInt(6) - pLevel.random.nextInt(6);
                    i1 += pLevel.random.nextInt(6) - pLevel.random.nextInt(6);
                    blockpos$mutableblockpos.set(l, i, i1);
                    double d0 = (double) l + 0.5D;
                    double d1 = (double) i1 + 0.5D;
                    Player player = pLevel.getNearestPlayer(d0, (double) i, d1, -1.0D, false);
                    if (player != null) {
                        double d2 = player.distanceToSqr(d0, (double) i, d1);

                        Optional<MobSpawnSettings.SpawnerData> optional = getRandomSpawnMobAt(pLevel, structuremanager, chunkgenerator, pCategory, pLevel.random, blockpos$mutableblockpos);
                        if (optional.isEmpty()) {
                            break;
                        }
                        mobspawnsettings$spawnerdata = optional.get();
                        if (mobs$isRightDistanceToPlayerAndSpawnPointSpecial(pLevel, pChunk, blockpos$mutableblockpos, d2, mobspawnsettings$spawnerdata)) {
                            k1 = mobspawnsettings$spawnerdata.minCount + pLevel.random.nextInt(1 + mobspawnsettings$spawnerdata.maxCount - mobspawnsettings$spawnerdata.minCount);

                            if (isValidSpawnPostitionForType(pLevel, pCategory, structuremanager, chunkgenerator, mobspawnsettings$spawnerdata, blockpos$mutableblockpos, d2) && pFilter.test(mobspawnsettings$spawnerdata.type, blockpos$mutableblockpos, pChunk)) {
                                Mob mob = getMobForSpawn(pLevel, mobspawnsettings$spawnerdata.type);
                                if (mob == null) {
                                    return;
                                }

                                mob.moveTo(d0, (double) i, d1, pLevel.random.nextFloat() * 360.0F, 0.0F);
                                if (isValidPositionForMob(pLevel, mob, d2)) {
                                    spawngroupdata = mob.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.NATURAL, spawngroupdata, (CompoundTag) null);
                                    ++j;
                                    ++l1;
                                    pLevel.addFreshEntityWithPassengers(mob);
                                    pCallback.run(mob, pChunk);
                                    if (j >= net.minecraftforge.event.ForgeEventFactory.getMaxSpawnPackSize(mob)) {
                                        return;
                                    }

                                    if (mob.isMaxGroupSizeReached(l1)) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    @Unique
    private static boolean mobs$isRightDistanceToPlayerAndSpawnPointSpecial(ServerLevel pLevel, ChunkAccess pChunk, BlockPos.MutableBlockPos pPos, double pDistance, MobSpawnSettings.SpawnerData spawnerData) {
        if (pDistance <= 576.0D) {
            return false;
        }
        if (pLevel.getSharedSpawnPos().closerToCenterThan(new Vec3((double) pPos.getX() + 0.5D, (double) pPos.getY(), (double) pPos.getZ() + 0.5D),
                spawnerData.type == Mobs.BLOCK_PRETENDER_ENTITY_TYPE.get() ? 8.0D : 24.0D)) {
            return false;
        } else {
            return Objects.equals(new ChunkPos(pPos), pChunk.getPos()) || pLevel.isNaturalSpawningAllowed(pPos);
        }
    }
}
