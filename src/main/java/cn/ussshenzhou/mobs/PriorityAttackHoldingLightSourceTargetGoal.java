package cn.ussshenzhou.mobs;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;

import static net.minecraft.world.item.Items.*;

/**
 * @author USS_Shenzhou
 */
public class PriorityAttackHoldingLightSourceTargetGoal extends NearestAttackableTargetGoal<Player> {

    public static final HashMap<Item, Integer> LIGHT_SOURCE_ITEM = new HashMap<>() {{
        List.of(BEACON, CAMPFIRE, GLOWSTONE, JACK_O_LANTERN, LAVA_BUCKET, SEA_LANTERN, CONDUIT, LANTERN, SHROOMLIGHT, OCHRE_FROGLIGHT, VERDANT_FROGLIGHT, PEARLESCENT_FROGLIGHT)
                .forEach(o -> put(o, 15 * 15));
        List.of(END_ROD, TORCH)
                .forEach(o -> put(o, 14 * 14));
        List.of(SOUL_TORCH, SOUL_LANTERN, SOUL_CAMPFIRE)
                .forEach(o -> put(o, 10 * 10));
        List.of(ENCHANTING_TABLE, ENDER_CHEST, REDSTONE_TORCH, GLOW_LICHEN)
                .forEach(o -> put(o, 7 * 7));
        List.of(MAGMA_BLOCK, CANDLE, WHITE_CANDLE, ORANGE_CANDLE, MAGENTA_CANDLE, LIGHT_BLUE_CANDLE, YELLOW_CANDLE, LIME_CANDLE, PINK_CANDLE, GRAY_CANDLE, LIGHT_GRAY_CANDLE, CYAN_CANDLE, PURPLE_CANDLE, BLUE_CANDLE, BROWN_CANDLE, GREEN_CANDLE, RED_CANDLE, BLACK_CANDLE)
                .forEach(o -> put(o, 3 * 3));
    }};

    public PriorityAttackHoldingLightSourceTargetGoal(Mob pMob) {
        super(pMob, Player.class, true);
        this.mustSee = true;
    }

    @Override
    protected void findTarget() {
        var players = mob.level().getNearbyEntities(Player.class, this.targetConditions, mob, mob.getBoundingBox().inflate(getFollowDistance() + 15));
        target = players.stream()
                .filter(player -> isLightSource(player.getItemInHand(InteractionHand.MAIN_HAND)) || isLightSource(player.getItemInHand(InteractionHand.OFF_HAND)))
                .min((p1, p2) -> (int) (getAttractionFactor(p1) - getAttractionFactor(p2)))
                .orElse(null);
    }

    private boolean isLightSource(ItemStack itemStack) {
        return LIGHT_SOURCE_ITEM.containsKey(itemStack.getItem());
    }

    private double getAttractionFactor(Player player) {
        return mob.distanceToSqr(player) - Math.max(LIGHT_SOURCE_ITEM.get(player.getItemInHand(InteractionHand.MAIN_HAND).getItem()), LIGHT_SOURCE_ITEM.get(player.getItemInHand(InteractionHand.OFF_HAND).getItem()));
    }
}
