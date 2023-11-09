package cn.ussshenzhou.mobs;

import cn.ussshenzhou.mobs.entity.BlockPretender;
import cn.ussshenzhou.mobs.entity.PlayerPretender;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeneralModBusListener {

    @SubscribeEvent
    public static void addAttackDamageToFriendly(EntityAttributeModificationEvent event) {
        GeneralForgeBusListener.FRIENDLY_TO_HOSTILE1.forEach(entityType -> event.add(entityType, Attributes.ATTACK_DAMAGE, 2));
    }

    @SubscribeEvent
    public static void addAttribute(EntityAttributeCreationEvent event) {
        event.put(Mobs.BLOCK_PRETENDER_ENTITY_TYPE.get(), BlockPretender.createAttributes().build());
        event.put(Mobs.PLAYER_PRETENDER_ENTITY_TYPE.get(), PlayerPretender.createAttributes().build());
    }

    @SubscribeEvent
    public static void addSpawn(SpawnPlacementRegisterEvent event) {
        event.register(Mobs.BLOCK_PRETENDER_ENTITY_TYPE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPretender::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(Mobs.PLAYER_PRETENDER_ENTITY_TYPE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PlayerPretender::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
    }

    @SubscribeEvent
    public static void loadConfig(FMLCommonSetupEvent event) {
        ConfigHelper.loadConfig(new MobsConfig());
    }
}
