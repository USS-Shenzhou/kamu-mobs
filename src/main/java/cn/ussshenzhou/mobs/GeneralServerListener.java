package cn.ussshenzhou.mobs;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
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
        addAll(List.of(BEE, DOLPHIN, FOX, IRON_GOLEM, LLAMA, WOLF, PANDA, POLAR_BEAR));
    }};

    @SubscribeEvent
    public static void modifyAiToHostile(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Mob mob) {
            if (NEUTRAL_TO_HOSTILE.contains(mob.getType())) {
                mob.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(mob, Player.class, false));
            }
        }
    }
}
