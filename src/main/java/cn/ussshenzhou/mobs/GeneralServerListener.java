package cn.ussshenzhou.mobs;

import cn.ussshenzhou.mobs.mixin.NearestAttackableTargetGoalAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.List;

import static net.minecraft.world.entity.EntityType.*;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GeneralServerListener {
    private static final HashSet<EntityType<?>> NEUTRAL_TO_HOSTILE = new HashSet<>() {{
        addAll(List.of(BEE, DOLPHIN, FOX, IRON_GOLEM, LLAMA, WOLF, PANDA, POLAR_BEAR,
                ENDERMAN, SPIDER, CAVE_SPIDER, ZOMBIFIED_PIGLIN));
    }};

    @SubscribeEvent
    public static void modifyNeutralToHostile(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Mob mob) {
            if (NEUTRAL_TO_HOSTILE.contains(mob.getType())) {
                revengeAndKeepHostileToPlayersGoals(mob);
            } else {
                vanillaGoalsIgnoreLineOfSight(mob);
            }
            addPriorityAttackHoldingLightSourceTargetGoal(mob);
        }
    }

    private static void vanillaGoalsIgnoreLineOfSight(Mob mob) {
        if (mob.getType() == ENDERMAN) {
            return;
        }
        for (WrappedGoal w : mob.targetSelector.getAvailableGoals()) {
            if (w.getGoal() instanceof NearestAttackableTargetGoal<?> n) {
                var g = ((NearestAttackableTargetGoalAccessor<?>) n);
                if (g.getTargetType() == Player.class) {
                    g.getTargetConditions().ignoreLineOfSight();
                }
            }
        }
    }

    private static void revengeAndKeepHostileToPlayersGoals(Mob mob) {
        var attackGoal = new NearestAttackableTargetGoal<>(mob, Player.class, false);
        if (mob.getType() == ENDERMAN) {
            //noinspection DataFlowIssue
            attackGoal.mustSee = true;
        } else {
            ((NearestAttackableTargetGoalAccessor<?>) attackGoal).getTargetConditions().ignoreLineOfSight();
        }
        mob.targetSelector.addGoal(-1, attackGoal);
        if (mob instanceof PathfinderMob pMob) {
            var revengeGoal = new HurtByTargetGoal(pMob);
            mob.targetSelector.addGoal(-3, revengeGoal);
        }
    }

    public static void addPriorityAttackHoldingLightSourceTargetGoal(Mob mob) {
        mob.targetSelector.addGoal(-2, new PriorityAttackHoldingLightSourceTargetGoal(mob));
    }

    @SubscribeEvent
    public static void allEvilRabbit(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Rabbit rabbit) {
            rabbit.setVariant(Rabbit.Variant.EVIL);
        }
    }

    public static final HashSet<EntityType<? extends Mob>> FRIENDLY_TO_HOSTILE1 = new HashSet<>() {{
        addAll(List.of(CAMEL, COW, DONKEY, HORSE, MOOSHROOM, PIG, SHEEP, SKELETON_HORSE, TURTLE, SNIFFER,
                BAT, CHICKEN, COD, SQUID, GLOW_SQUID, PARROT, SALMON, TROPICAL_FISH, STRIDER));
    }};

    public static final HashSet<EntityType<?>> FRIENDLY_TO_HOSTILE2 = new HashSet<>() {{
        addAll(List.of(CAT, OCELOT, SNOW_GOLEM));
    }};

    @SubscribeEvent
    public static void modifyFriendlyToHostile(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof PathfinderMob animal) {
            if (FRIENDLY_TO_HOSTILE1.contains(animal.getType()) || FRIENDLY_TO_HOSTILE2.contains(animal.getType())) {
                animal.targetSelector.addGoal(-3, new HurtByTargetGoal(animal));
                animal.targetSelector.addGoal(-2, new PriorityAttackHoldingLightSourceTargetGoal(animal));
                animal.targetSelector.addGoal(-1, new NearestAttackableTargetGoal<>(animal, Player.class, false));
            }
            if (FRIENDLY_TO_HOSTILE1.contains(animal.getType())) {
                //noinspection DataFlowIssue
                animal.getAttribute(Attributes.ARMOR).setBaseValue(4);
                animal.goalSelector.addGoal(-2, new MeleeAttackGoal(animal, 1, true));
                animal.goalSelector.addGoal(-1, new MoveTowardsTargetGoal(animal, 1, 32));
            }
        }
    }
}
