package cn.ussshenzhou.mobs;

import cn.ussshenzhou.mobs.ai.*;
import cn.ussshenzhou.mobs.mixin.NearestAttackableTargetGoalAccessor;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.ForgeGameTestHooks;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static net.minecraft.world.entity.EntityType.*;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GeneralServerListener {
    private static final HashSet<EntityType<?>> NEUTRAL_TO_HOSTILE = new HashSet<>() {{
        addAll(List.of(BEE, DOLPHIN, FOX, IRON_GOLEM, LLAMA, TRADER_LLAMA, WOLF, PANDA, POLAR_BEAR,
                SPIDER, CAVE_SPIDER, ZOMBIFIED_PIGLIN));
    }};
    public static final HashSet<EntityType<? extends Mob>> FRIENDLY_TO_HOSTILE1 = new HashSet<>() {{
        addAll(List.of(CAMEL, COW, DONKEY, HORSE, MOOSHROOM, PIG, SHEEP, SKELETON_HORSE, TURTLE, SNIFFER,
                BAT, CHICKEN, COD, SQUID, GLOW_SQUID, PARROT, SALMON, TROPICAL_FISH, STRIDER, VILLAGER, WANDERING_TRADER));
    }};
    public static final HashSet<EntityType<?>> FRIENDLY_TO_HOSTILE2 = new HashSet<>() {{
        addAll(List.of(CAT, OCELOT, SNOW_GOLEM, FROG, AXOLOTL));
    }};
    public static final HashSet<EntityType<? extends PathfinderMob>> ATTRACTED_BY_LIGHT = new HashSet<>() {{
        addAll(List.of(ZOMBIE, DROWNED, HUSK, SKELETON, STRAY, SPIDER, CAVE_SPIDER, ZOMBIE_VILLAGER, CREEPER));
    }};
    public static final HashSet<EntityType<? extends PathfinderMob>> JUMP_ABLE = new HashSet<>() {{
        addAll(List.of(ZOMBIE, HUSK, ZOMBIE_VILLAGER, ZOMBIFIED_PIGLIN, PIGLIN, PIGLIN_BRUTE));
    }};
    public static final HashSet<EntityType<? extends PathfinderMob>> BREAK_BLOCK = new HashSet<>() {{
        addAll(List.of(ZOMBIE, HUSK));
    }};
    public static final List<MobSpawnSettings.SpawnerData> ALL_SPAWNER_DATA = new ArrayList<>();
    public static final HashMap<EntityType<?>, Integer> ALL_POTENTIAL_SPAWNS = new HashMap<>();

    static {
        putAllPotential(80, CREEPER, SKELETON, ZOMBIE, SPIDER);
        putAllPotential(60, PILLAGER, STRAY, HUSK, VINDICATOR);
        putAllPotential(30, DROWNED, SILVERFISH, ZOMBIFIED_PIGLIN);
        putAllPotential(5, EVOKER, WITCH, WITHER_SKELETON, RAVAGER, ILLUSIONER, SLIME);
        ALL_POTENTIAL_SPAWNS.forEach((type, weight) -> ALL_SPAWNER_DATA.add(spawnerData(type, weight)));
    }

    public static final List<MobSpawnSettings.SpawnerData> OVER_WORLD_SPAWNER_DATA = new ArrayList<>();
    public static final HashMap<EntityType<?>, Integer> OVER_WORLD_POTENTIAL_SPAWNS = new HashMap<>();

    static {
        putAdditionalPotential(OVER_WORLD_POTENTIAL_SPAWNS, 20, SHULKER);
        OVER_WORLD_POTENTIAL_SPAWNS.forEach((type, weight) -> OVER_WORLD_SPAWNER_DATA.add(spawnerData(type, weight)));
    }

    public static final List<MobSpawnSettings.SpawnerData> NETHER_SPAWNER_DATA = new ArrayList<>();
    public static final HashMap<EntityType<?>, Integer> NETHER_POTENTIAL_SPAWNS = new HashMap<>();

    static {
        putAdditionalPotential(NETHER_POTENTIAL_SPAWNS, 50, HOGLIN, ZOMBIFIED_PIGLIN);
        putAdditionalPotential(NETHER_POTENTIAL_SPAWNS, 20, PIGLIN_BRUTE);
        NETHER_POTENTIAL_SPAWNS.forEach((type, weight) -> NETHER_SPAWNER_DATA.add(spawnerData(type, weight)));
    }

    public static final List<MobSpawnSettings.SpawnerData> END_SPAWNER_DATA = new ArrayList<>();
    public static final HashMap<EntityType<?>, Integer> END_POTENTIAL_SPAWNS = new HashMap<>();

    static {
        putAdditionalPotential(END_POTENTIAL_SPAWNS, 500, ENDERMAN);
        putAdditionalPotential(END_POTENTIAL_SPAWNS, 100, SHULKER);
        END_POTENTIAL_SPAWNS.forEach((type, weight) -> END_SPAWNER_DATA.add(spawnerData(type, weight)));
    }

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

    @SuppressWarnings("DataFlowIssue")
    @SubscribeEvent
    public static void modifyFriendlyToHostile(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof PathfinderMob animal) {
            if (FRIENDLY_TO_HOSTILE1.contains(animal.getType()) || FRIENDLY_TO_HOSTILE2.contains(animal.getType())) {
                animal.targetSelector.addGoal(-3, new HurtByTargetGoal(animal));
                animal.targetSelector.addGoal(-2, new PriorityAttackHoldingLightSourceTargetGoal(animal));
                animal.targetSelector.addGoal(-1, new NearestAttackableTargetGoal<>(animal, Player.class, false));
            }
            if (FRIENDLY_TO_HOSTILE1.contains(animal.getType())) {
                try {
                    if (animal.getAttribute(Attributes.ARMOR).getBaseValue() < 4) {
                        animal.getAttribute(Attributes.ARMOR).setBaseValue(4);
                    }
                    animal.goalSelector.addGoal(-3, new MeleeAttackGoal(animal, 1, true));
                    animal.goalSelector.addGoal(-1, new MoveTowardsTargetGoal(animal, 0.8, 32));
                    if (animal instanceof AbstractVillager villager) {
                        villager.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4);
                        villager.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(villager.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() * 0.8);
                    }
                } catch (NullPointerException ignored) {
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

    @SubscribeEvent
    public static void mobsAttractedByLight(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getLevel().dimension() != Level.NETHER && event.getEntity() instanceof PathfinderMob mob) {
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

    @SubscribeEvent
    public static void addJumpGoal(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof PathfinderMob mob) {
            if (JUMP_ABLE.contains(mob.getType())) {
                mob.goalSelector.addGoal(1, new JumpToTargetGoal(mob));
            }
        }
    }

    @SubscribeEvent
    public static void addBreakGoal(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof PathfinderMob mob) {
            if (BREAK_BLOCK.contains(mob.getType())) {
                mob.goalSelector.addGoal(1, new BreakBlockGoal(mob));
            }
        }
    }

    /*public static final HashMap<EntityType<?>, Integer> OVER_WORLD_POTENTIAL_SPAWNS = new HashMap<>() {{
        GeneralServerListener.putAll(80, CREEPER, SKELETON, ZOMBIE, SPIDER);
        GeneralServerListener.putAll(60, PILLAGER, STRAY, HUSK, VINDICATOR, ZOMBIFIED_PIGLIN);
        GeneralServerListener.putAll(40, DROWNED, RAVAGER, SILVERFISH, SLIME, ILLUSIONER);
        GeneralServerListener.putAll(20, EVOKER, SHULKER, WITCH, WITHER_SKELETON, ZOGLIN);
    }};*/

    private static void putAllPotential(int weight, EntityType<?>... types) {
        Arrays.stream(types).forEach(t -> ALL_POTENTIAL_SPAWNS.put(t, weight));
    }

    private static void putAdditionalPotential(HashMap<EntityType<?>, Integer> map, int weight, EntityType<?>... types) {
        Arrays.stream(types).forEach(t -> map.put(t, weight));
    }

    public static MobSpawnSettings.SpawnerData spawnerData(EntityType<?> type, int weight) {
        return new MobSpawnSettings.SpawnerData(type, weight, 1, 1);
    }

    @SubscribeEvent
    public static void addSpawnEntityToAllBiome(LevelEvent.PotentialSpawns event) {
        if (event.getMobCategory() != MobCategory.MONSTER) {
            return;
        }
        var list = event.getSpawnerDataList();
        var nativeTypes = list.stream()
                .map(spawnerData -> spawnerData.type)
                .collect(Collectors.toSet());
        ALL_SPAWNER_DATA.stream()
                .filter(spawnerData -> !nativeTypes.contains(spawnerData.type))
                .forEach(event::addSpawnerData);

        var level = event.getLevel();
        if (((Level) level).dimension() == Level.OVERWORLD) {
            OVER_WORLD_SPAWNER_DATA.stream()
                    //.filter(spawnerData -> !nativeTypes.contains(spawnerData.type))
                    .forEach(event::addSpawnerData);
        } else if (((Level) level).dimension() == Level.NETHER) {
            NETHER_SPAWNER_DATA.stream()
                    //.filter(spawnerData -> !nativeTypes.contains(spawnerData.type))
                    .forEach(event::addSpawnerData);
        } else if (((Level) level).dimension() == Level.END) {
            //event.removeSpawnerData(list.stream().filter(spawnerData -> spawnerData.type == ENDERMAN).findFirst().orElse(null));
            END_SPAWNER_DATA.stream()
                    //.filter(spawnerData -> !nativeTypes.contains(spawnerData.type))
                    .forEach(event::addSpawnerData);
        }

        var pos = event.getPos();
        var biome = level.getBiome(pos);
        if (biome.is(Tags.Biomes.IS_CAVE)) {
            list.add(spawnerData(CAVE_SPIDER, 40));
        }
        if (biome.is(Tags.Biomes.IS_WATER)) {
            list.add(spawnerData(GUARDIAN, 40));
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        var level = event.getEntity().level();
        if (level.isClientSide()) {
            return;
        }
        var player = (ServerPlayer) event.getEntity();
        checkAndAward(event, player, "KaMuaMua", "nether_new_neighbors", (e, p) -> e.getTo() == Level.NETHER);
        checkAndAward(event, player, "BadCen", "end_new_neighbors", (e, p) -> e.getTo() == Level.END);
    }

    private static <T> void checkAndAward(T context, ServerPlayer player, String playerName, String advancementName, BiPredicate<T, ServerPlayer> predicate) {
        var n = player.getGameProfile().getName();
        if (n.equals(playerName) || "Dev".equals(n)) {
            if (predicate.test(context, player)) {
                var a = player.serverLevel().getServer().getAdvancements().getAdvancement(new ResourceLocation("minecraft:story/" + advancementName));
                if (a != null) {
                    player.getAdvancements().award(a, "custom");
                }
            }
        }
    }
}
