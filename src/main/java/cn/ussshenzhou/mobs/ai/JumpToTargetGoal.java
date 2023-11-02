package cn.ussshenzhou.mobs.ai;

import cn.ussshenzhou.mobs.GeneralServerListener;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.MutableTriple;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Modified from <a href="https://github.com/miyo6032/mobs-attempt-parkour/blob/1.20/src/main/kotlin/net/barribob/parkour/ai/JumpToTargetGoal.kt">mobs-attempt-parkour</a> under MIT license.
 * <p>
 * Jumping AI by Barribob
 * <p>
 * What it does
 * Detects gaps and compulsively makes entities jump over them if they are in the general direction of the target
 * Will detect water, lava, and fire as well.
 * <p>
 * What it does not do
 * Does not employ any actual path finding, so it's not a true jumping navigation ai
 * Thus it can't do complex parkour to get to a target
 * <p>
 * Known Issues
 * Spider navigation makes it so that spiders speed off in a straight direction
 * Jump calculations start to overestimate the distance with high velocities... mostly because Minecraft has a strangely high air resistance effect going on
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
public class JumpToTargetGoal extends Goal {
    private final Mob entity;
    private final double minTargetDistance = 1.5; // Minimum distance required for the jump AI to activate
    private final double jumpClearanceAboveHead = 1.0; // Y offset above an entity's hitbox to raycast to see if there are any blocks in the way of the jump
    private final int forwardMovementTicks = 40; // How many ticks the entity will "press the forward key" while jumping
    private final List<Integer> anglesToAttemptJump = IntStream.rangeClosed(-45, 45).filter(i -> i % 5 == 0).boxed().toList();
    private final double edgeDetectionDistance = 2.0; // Maximum distance an entity can be from an edge before the AI considers running
    private final int detectionPoints = (int) Math.floor(edgeDetectionDistance * 8);
    private final double moveSpeed = 1.0;
    private final double moveFactor = 3.0;
    private final double jumpForwardSpeed = 7.0;
    private final double gravity = 0.1;
    private final double yVelocityScale = 1.53;
    private final double jumpNoise = 0.1;
    private final int maxTicksAttemptNavigation = 40;
    private JumpData jumpData;

    public JumpToTargetGoal(Mob entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    public static final class JumpData {
        private Tuple<Double, Double> jumpVel;
        private Vec3 direction;
        private Vec3 edgePos;
        private int ticksAttempted;

        public JumpData(Tuple<Double, Double> jumpVel, Vec3 direction, Vec3 edgePos, int ticksAttempted) {
            this.jumpVel = jumpVel;
            this.direction = direction;
            this.edgePos = edgePos;
            this.ticksAttempted = ticksAttempted;
        }

        public JumpData(Tuple<Double, Double> jumpVel, Vec3 direction, Vec3 edgePos) {
            this(jumpVel, direction, edgePos, 0);
        }

        public Tuple<Double, Double> jumpVel() {
            return jumpVel;
        }

        public Vec3 direction() {
            return direction;
        }

        public Vec3 edgePos() {
            return edgePos;
        }

        public int ticksAttempted() {
            return ticksAttempted;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (JumpData) obj;
            return Objects.equals(this.jumpVel, that.jumpVel) &&
                    Objects.equals(this.direction, that.direction) &&
                    Objects.equals(this.edgePos, that.edgePos) &&
                    this.ticksAttempted == that.ticksAttempted;
        }

        @Override
        public int hashCode() {
            return Objects.hash(jumpVel, direction, edgePos, ticksAttempted);
        }

        @Override
        public String toString() {
            return "JumpData[" +
                    "jumpVel=" + jumpVel + ", " +
                    "direction=" + direction + ", " +
                    "edgePos=" + edgePos + ", " +
                    "ticksAttempted=" + ticksAttempted + ']';
        }

    }

    @Override
    public boolean canUse() {
        if (entity.getNavigation() != null && entity.onGround() && jumpData == null) {
            if (entity.getNavigation().getPath() == null) {
                return false;
            }

            List<BlockType> obstacles = new ArrayList<>();
            for (int i = entity.getNavigation().getPath().getNextNodeIndex(); i < entity.getNavigation().getPath().getNodeCount(); i++) {
                BlockPos pos = floorBlockPos(entity.getNavigation().getPath().getNode(i).asVec3());
                BlockType blockType = getNode(pos);
                obstacles.add(blockType);
                if (blockType == BlockType.PASSABLE_OBSTACLE || blockType == BlockType.SOLID_OBSTACLE) {
                    return false;
                }
            }

            if (entity.getNavigation().isInProgress() && entity.getNavigation().getPath().canReach() && obstacles.stream().noneMatch(b -> b == BlockType.WALKABLE || b == BlockType.PASSABLE_OBSTACLE)) {
                return false;
            }

            Vec3 target = asVec3(entity.getNavigation().getTargetPos()).add(0.5, 0, 0.5);
            if (target == null || target.distanceTo(entity.position()) < minTargetDistance) {
                return false;
            }

            JumpData jumpData = findJump(target);

            if (jumpData != null) {
                this.jumpData = jumpData;
                return true;
            }
        }
        return jumpData != null;
    }

    private JumpData findJump(Vec3 target) {
        var v = new Vec3(0, 1, 0);
        var targetDirection = target.subtract(entity.position()).subtract(v.scale(target.subtract(entity.position()).dot(v))).normalize();
        for (int angle : anglesToAttemptJump) {
            Vec3 jumpDirection = rotateVector(targetDirection, new Vec3(0, 1, 0), angle);
            List<Tuple<Vec3, BlockType>> gaps = new ArrayList<>();
            Vec3 endPos = entity.position().add(jumpDirection.scale(edgeDetectionDistance));

            if (entity.getDeltaMovement().add(jumpDirection).lengthSqr() < jumpDirection.lengthSqr()) {
                continue;
            }

            lineCallback(entity.position(), endPos, detectionPoints, (pos, ignored) -> gaps.add(new Tuple<>(pos, getNode(floorBlockPos(pos)))));

            Tuple<Tuple<Vec3, BlockType>, Tuple<Vec3, BlockType>> pairs = null;
            Tuple<Tuple<Vec3, BlockType>, Tuple<Vec3, BlockType>> hasGapsInARow = null;
            for (int i = 0; i < gaps.size() - 1; i++) {
                var o1 = gaps.get(i);
                var o2 = gaps.get(i + 1);
                if (o1.getB() == BlockType.WALKABLE && o2.getB() == BlockType.PASSABLE_OBSTACLE) {
                    pairs = new Tuple<>(o1, o2);
                    break;
                }
            }
            for (int i = 0; i < gaps.size() - 1; i++) {
                var o1 = gaps.get(i);
                var o2 = gaps.get(i + 1);
                if (o1.getB() == BlockType.WALKABLE && o2.getB() == BlockType.WALKABLE) {
                    hasGapsInARow = new Tuple<>(o1, o2);
                    break;
                }
            }
            if (pairs != null && hasGapsInARow != null) {
                var dirAndVel = getJumpLength(pairs.getB().getA(), jumpDirection);

                if (dirAndVel != null) {
                    return new JumpData(dirAndVel.getB(), dirAndVel.getA(), pairs.getB().getA());
                }
            }
        }

        return null;
    }

    @Override
    public void tick() {
        if (jumpData == null) {
            return;
        }

        jumpData.ticksAttempted += 1;

        if (jumpData.ticksAttempted > maxTicksAttemptNavigation) {
            entity.getNavigation().stop();
            this.jumpData = null;
            return;
        }

        if (floorBlockPos(entity.position()).equals(floorBlockPos(jumpData.edgePos))) {
            jump(jumpData);
        } else {
            entity.getMoveControl().setWantedPosition(jumpData.edgePos.x, jumpData.edgePos.y, jumpData.edgePos.z, moveSpeed);
        }
    }

    private void jump(JumpData jumpData) {
        Vec3 jumpDirection = entity.position().add(jumpData.direction);
        double xVelocity = jumpData.jumpVel.getA() + jumpData.jumpVel.getA() * (Math.random() - 0.5) * 2 * jumpNoise;
        double yVelocity = (jumpData.jumpVel.getB() > 0) ? 0.0 : 0.1;
        leapTowards(entity, jumpDirection, xVelocity, yVelocity);

        if (jumpData.jumpVel.getB() > 0) {
            entity.getJumpControl().jump();
        }

        Predicate<Mob> shouldCancel = mob -> !mob.isAlive() || mob.onGround();
        GeneralServerListener.TASKS.add(new GeneralServerListener.repeatableExecute<>(
                shouldCancel, entity,
                () -> {
                    Vec3 movePos = entity.position().add(jumpData.direction.scale(3));
                    if (!entity.onGround()) {
                        entity.getMoveControl().setWantedPosition(movePos.x, movePos.y, movePos.z, jumpForwardSpeed);
                    }
                }, forwardMovementTicks));
        entity.getNavigation().stop();
        this.jumpData = null;
    }

    private MutableTriple<Double, Integer, Double> getMobJumpAbilities() {
        double jumpYVel = getJumpVelocity(entity.level(), entity); // Maximum y velocity for a jump. Used in determining if an entity can make a jump
        int maxJumpHeight = (int) (jumpYVel * 4);
        double maxHorizontalVelocity = entity.getAttributeValue(Attributes.MOVEMENT_SPEED) * moveSpeed * moveFactor;
        return new MutableTriple<>(jumpYVel, maxJumpHeight, maxHorizontalVelocity);
    }

    private List<Tuple<Integer, Integer>> getJumpOffsets(double maxHorzVel) {
        int steps;
        if (maxHorzVel < 0.24) {
            steps = 3;
        } else if (maxHorzVel > 0.24 && maxHorzVel < 0.3) {
            steps = 4;
        } else {
            steps = 5;
        }

        List<Tuple<Integer, Integer>> jumpOffsets = new ArrayList<>();
        for (int d = 0; d <= steps; d++) {
            for (int y = 0; y <= steps - d; y++) {
                jumpOffsets.add(new Tuple<>(d, y));
            }
        }
        jumpOffsets.sort(Comparator.comparing(pair -> pair.getA() + pair.getB()));

        return jumpOffsets;
    }

    private Tuple<Vec3, Tuple<Double, Double>> getJumpLength(Vec3 actorPos, Vec3 targetDirection) {
        MutableTriple<Double, Integer, Double> abilities = getMobJumpAbilities();
        double maxYVel = abilities.getLeft();
        double maxJumpHeight = abilities.getMiddle();
        double maxHorzVel = abilities.getRight();

        List<Tuple<Integer, Integer>> jumpOffsets = getJumpOffsets(maxHorzVel);

        for (Tuple<Integer, Integer> offset : jumpOffsets) {
            double scaledStepX = 1.0 + offset.getA();
            var jumpToPos = actorPos.add(targetDirection.scale(scaledStepX));
            var blockPos = floorBlockPos(jumpToPos);
            var groundHeight = findGroundAt(blockPos, offset.getB(), (int) maxJumpHeight);
            if (groundHeight == null) {
                continue;
            }

            var walkablePos = new BlockPos(blockPos.getX(), groundHeight, blockPos.getZ());
            //BlockType blockType = getNode(walkablePos);
            var blockShape = entity.level().getBlockState(walkablePos).getCollisionShape(entity.level(), walkablePos);
            var offsetPos = actorPos.subtract(asVec3(walkablePos));
            var cornerPos = findClosestCorner(offsetPos, blockShape, 16);
            if (cornerPos == null) {
                continue;
            }
            cornerPos.add(asVec3(walkablePos));

            Vec3 horizontalJumpPos = new Vec3(cornerPos.x, actorPos.y, cornerPos.z);

            double jumpLength = horizontalJumpPos.subtract(actorPos).length() - (entity.getBbWidth() * 0.5);
            //FIXME
            Vec3 recalculatedDirection = horizontalJumpPos.subtract(actorPos).normalize();

            if (!hasClearance(actorPos, jumpLength, targetDirection)) {
                return null;
            }

            double blockHeight = (!blockShape.isEmpty()) ?
                    blockShape.bounds().getYsize() : 0.0;
            double jumpHeight = groundHeight + blockHeight - actorPos.y;

            for (double jumpEffort : Arrays.asList(0.0, maxYVel)) {
                double xVelWithJump = calculateRequiredXVelocity(jumpLength, jumpHeight, jumpEffort);

                if (xVelWithJump < maxHorzVel) {
                    return new Tuple<>(recalculatedDirection, new Tuple<>(xVelWithJump, jumpEffort));
                }
            }
        }
        return null;
    }

    private double calculateRequiredXVelocity(double jumpLength, double jumpHeight, double yVel) {
        double scaledYVel = yVel * yVelocityScale;
        double quadraticSqrt = sqrt(pow(scaledYVel, 2) - 4 * -jumpHeight * -gravity);

        if (Double.isNaN(quadraticSqrt)) {
            return Double.POSITIVE_INFINITY;
        }

        double numerator = (-scaledYVel > 0) ? -scaledYVel + quadraticSqrt : -scaledYVel - quadraticSqrt;
        double time = numerator / (2 * -gravity);

        if (time == 0.0) {
            return Double.POSITIVE_INFINITY;
        }

        return jumpLength / time;
    }

    /**
     * Finds the first y position that has walkable ground or none
     */
    private Integer findGroundAt(BlockPos pos, int height, int maxJumpHeight) {
        List<Integer> range = new ArrayList<>();
        for (int i = -height; i <= maxJumpHeight; i++) {
            range.add(i);
        }
        Integer walkablePos = range.stream()
                .filter(i -> getNode(pos.above(i)) == BlockType.WALKABLE)
                .findFirst()
                .orElse(null);
        return (walkablePos != null) ? walkablePos + pos.getY() - 1 : null;
    }

    /**
     * Finds if there are any blocks above the entity that may block the jump
     */
    private boolean hasClearance(Vec3 actorPos, double jumpLength, Vec3 jumpDirection) {
        double requiredHeight = entity.getBbHeight() + jumpClearanceAboveHead;
        Vec3 start = actorPos.add(0, requiredHeight, 0);
        Vec3 end = start.add(jumpDirection.scale(jumpLength));
        HitResult result = entity.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        return result.getType() == HitResult.Type.MISS;
    }

    private BlockType getNode(BlockPos pos) {
        return getBlockType(entity.level(), pos, 2);
    }

    public enum BlockType {
        OPEN,
        BLOCKED,
        SOLID_OBSTACLE,
        PASSABLE_OBSTACLE,
        WALKABLE
    }

    public static Vec3 rotateVector(Vec3 v, Vec3 k, double degrees) {
        double theta = Math.toRadians(degrees);
        k = k.normalize();
        return v
                .scale(Math.cos(theta))
                .add(k.cross(v)
                        .scale(Math.sin(theta)))
                .add(k.scale(k.dot(v))
                        .scale(1 - Math.cos(theta)));
    }

    public static Vec3 asVec3(BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static void lineCallback(Vec3 start, Vec3 end, int points, LineCallback callback) {
        Vec3 dir = end.subtract(start).scale(1.0 / (points - 1.0));
        Vec3 pos = start;
        for (int i = 0; i < points; i++) {
            //callback.callback(new Vec3(pos.x, pos.y, pos.z), i);
            callback.callback(pos, i);
            pos = pos.add(dir);
        }
    }

    public interface LineCallback {
        void callback(Vec3 pos, int index);
    }

    public static double getJumpVelocity(Level world, LivingEntity entity) {
        double baseVelocity = 0.42 * getJumpVelocityMultiplier(world, entity);
        if (entity.hasEffect(MobEffects.JUMP)) {
            baseVelocity += 0.1 * (entity.getEffect(MobEffects.JUMP).getAmplifier() + 1);
        }
        return baseVelocity;
    }

    private static double getJumpVelocityMultiplier(Level world, LivingEntity entity) {
        float f = world.getBlockState(entity.blockPosition()).getBlock().getJumpFactor();
        float g = world.getBlockState(getVelocityAffectingPos(entity)).getBlock().getJumpFactor();
        return (f == 1.0) ? g : f;
    }

    private static BlockPos getVelocityAffectingPos(LivingEntity entity) {
        return floorBlockPos(entity.getX(), entity.getBoundingBox().minY - 0.5000001, entity.getZ());
    }

    private static BlockPos floorBlockPos(double x, double y, double z) {
        return new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }

    public static BlockPos floorBlockPos(Vec3 pos) {
        return floorBlockPos(pos.x, pos.y, pos.z);
    }

    public static void leapTowards(LivingEntity entity, Vec3 target, double horzVel, double yVel) {
        Vec3 dir = target.subtract(entity.position()).normalize();
        Vec3 leap = new Vec3(dir.x, 0.0, dir.z).normalize().scale(horzVel).add(0, yVel, 0);
        double clampedYVelocity = (entity.getDeltaMovement().y < 0.1) ? leap.y : 0.0;

        Vec3 horzVelocity = entity.getDeltaMovement().add(leap.x, 0.0, leap.z);
        double scale = horzVel / horzVelocity.length();
        if (scale < 1) {
            horzVelocity.scale(scale);
        }
        horzVelocity.add(0, clampedYVelocity, 0);
        entity.setDeltaMovement(horzVelocity);
    }

    public static BlockType getBlockType(BlockGetter world, BlockPos pos, int callsLeft) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        FluidState fluidState = world.getFluidState(pos);
        BlockType belowType = (pos.getY() > 0 && callsLeft > 0) ? getBlockType(world, pos.below(), callsLeft - 1) : BlockType.OPEN;

        if (blockState.is(Blocks.SWEET_BERRY_BUSH) ||
                blockState.is(BlockTags.FIRE) ||
                CampfireBlock.isLitCampfire(blockState) ||
                fluidState.is(FluidTags.WATER)) {
            return BlockType.PASSABLE_OBSTACLE;
        } else if (fluidState.is(FluidTags.LAVA) ||
                blockState.is(Blocks.CACTUS) ||
                blockState.is(Blocks.HONEY_BLOCK) ||
                blockState.is(Blocks.MAGMA_BLOCK)) {
            return BlockType.SOLID_OBSTACLE;
        } else if (block instanceof LeavesBlock ||
                blockState.is(BlockTags.FENCES) ||
                blockState.is(BlockTags.WALLS) ||
                (block instanceof FenceGateBlock && !blockState.getValue(FenceGateBlock.OPEN)) ||
                (block instanceof DoorBlock && !blockState.getValue(DoorBlock.OPEN)) ||
                (block instanceof DoorBlock && blockState.getValue(DoorBlock.OPEN)) ||
                !blockState.isPathfindable(world, pos, PathComputationType.LAND)) {
            return BlockType.BLOCKED;
        } else if (belowType == BlockType.BLOCKED) {
            return BlockType.WALKABLE;
        } else if (belowType == BlockType.OPEN) {
            return BlockType.PASSABLE_OBSTACLE;
        } else if (belowType == BlockType.PASSABLE_OBSTACLE) {
            return BlockType.PASSABLE_OBSTACLE;
        } else if (belowType == BlockType.SOLID_OBSTACLE) {
            return BlockType.PASSABLE_OBSTACLE;
        } else {
            return BlockType.OPEN;
        }
    }

    public static Vec3 findClosestCorner(Vec3 point, VoxelShape shape, int maxSamples) {
        var l = shape.toAabbs().stream()
                .flatMap(box -> getTopCornersAndEdges(box).stream()).collect(Collectors.toList());
        Collections.shuffle(l);
        return l.stream()
                .limit(maxSamples)
                .min(Comparator.comparingDouble(a -> a.distanceToSqr(point)))
                .orElse(null);
    }

    private static java.util.List<Vec3> getTopCornersAndEdges(AABB box) {
        double halfX = box.getXsize() * 0.5;
        double halfZ = box.getZsize() * 0.5;

        return java.util.List.of(
                new Vec3(box.minX, box.maxY, box.minZ),
                new Vec3(box.maxX, box.maxY, box.minZ),
                new Vec3(box.minX, box.maxY, box.maxZ),
                new Vec3(box.maxX, box.maxY, box.maxZ),
                new Vec3(box.minX + halfX, box.maxY, box.minZ),
                new Vec3(box.minX, box.maxY, box.minZ + halfZ),
                new Vec3(box.maxX, box.maxY, box.minZ + halfZ),
                new Vec3(box.minX + halfX, box.maxY, box.maxZ)
        );
    }

}
