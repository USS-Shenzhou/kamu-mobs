package cn.ussshenzhou.mobs;

import cn.ussshenzhou.mobs.ai.*;
import cn.ussshenzhou.mobs.mixin.NearestAttackableTargetGoalAccessor;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.function.Predicate;

import static net.minecraft.world.entity.EntityType.*;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GeneralServerListener {
    private static final HashSet<EntityType<?>> NEUTRAL_TO_HOSTILE = new HashSet<>() {{
        addAll(List.of(BEE, DOLPHIN, FOX, IRON_GOLEM, LLAMA, TRADER_LLAMA, WOLF, PANDA, POLAR_BEAR,
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
                BAT, CHICKEN, COD, SQUID, GLOW_SQUID, PARROT, SALMON, TROPICAL_FISH, STRIDER, VILLAGER, WANDERING_TRADER));
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
                if (animal.getAttribute(Attributes.ARMOR).getBaseValue() < 4) {
                    //noinspection DataFlowIssue
                    animal.getAttribute(Attributes.ARMOR).setBaseValue(4);
                }
                animal.goalSelector.addGoal(-3, new MeleeAttackGoal(animal, 1, true));
                animal.goalSelector.addGoal(-1, new MoveTowardsTargetGoal(animal, 0.8, 32));
                if (animal instanceof AbstractVillager villager) {
                    villager.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4);
                    villager.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(villager.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() * 0.8);
                }
            }
        }
    }

    /*@SubscribeEvent
    public static void giveVillagerWeapon(MobSpawnEvent.FinalizeSpawn event) {
        //villager's brain won't use weapon simply.
        if (event.getEntity() instanceof AbstractVillager abstractVillager) {
            var i = new ItemStack(Items.STONE_AXE);
            abstractVillager.getInventory().addItem(i);
            abstractVillager.setItemSlot(EquipmentSlot.MAINHAND, i);
            abstractVillager.goalSelector.addGoal(-2, new MoveTowardsTargetGoal(abstractVillager, 0.6, 32));
        }
    }*/

    public static final HashSet<EntityType<? extends PathfinderMob>> ATTRACTED_BY_LIGHT = new HashSet<>() {{
        addAll(List.of(ZOMBIE, DROWNED, HUSK, SKELETON, STRAY, SPIDER, CAVE_SPIDER, ZOMBIE_VILLAGER, CREEPER));
    }};

    @SubscribeEvent
    public static void mobsAttractedByLight(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof PathfinderMob mob) {
            if (ATTRACTED_BY_LIGHT.contains(mob.getType())) {
                mob.goalSelector.addGoal(5, new AttractedByLightSourceGoal(mob));
            }
        }
    }

    public static final LinkedList<repeatableExecute<?>> TASKS = new LinkedList<>();

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Iterator<repeatableExecute<?>> iterator = TASKS.iterator();
            while (iterator.hasNext()) {
                repeatableExecute<?> task = iterator.next();
                task.tick();
                if (task.duration <= 0) {
                    iterator.remove();
                }
            }
        }
    }

    public static class repeatableExecute<T> {
        Runnable task;
        int duration;
        Predicate<T> checker;
        T checked;

        public repeatableExecute(Predicate<T> checker, T checked, Runnable task, int duration) {
            this.task = task;
            this.duration = duration;
            this.checker = checker;
            this.checked = checked;
        }

        public void tick() {
            if (checker.test(checked)) {
                task.run();
                duration--;
            }
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static final HashSet<EntityType<? extends PathfinderMob>> JUMPABLE = new HashSet<>() {{
        addAll(List.of(ZOMBIE, HUSK, ZOMBIE_VILLAGER, ZOMBIFIED_PIGLIN, PIGLIN, PIGLIN_BRUTE));
    }};

    @SubscribeEvent
    public static void addJumpGoal(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof PathfinderMob mob) {
            if (JUMPABLE.contains(mob.getType())) {
                mob.goalSelector.addGoal(1, new JumpToTargetGoal(mob));
            }
        }
    }

    public static final HashSet<EntityType<? extends PathfinderMob>> BREAK_BLOCK = new HashSet<>() {{
        addAll(List.of(ZOMBIE, HUSK));
    }};

    @SubscribeEvent
    public static void addBreakGoal(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof PathfinderMob mob) {
            if (BREAK_BLOCK.contains(mob.getType())) {
                mob.goalSelector.addGoal(1, new BreakBlockGoal(mob));
            }
        }
    }
}
