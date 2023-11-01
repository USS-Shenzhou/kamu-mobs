package cn.ussshenzhou.mobs;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeneralModBusListener {

    @SubscribeEvent
    public static void addAttackDamageToFriendly(EntityAttributeModificationEvent event) {
        GeneralServerListener.FRIENDLY_TO_HOSTILE1.forEach(entityType -> event.add(entityType, Attributes.ATTACK_DAMAGE, 2));
    }
}
