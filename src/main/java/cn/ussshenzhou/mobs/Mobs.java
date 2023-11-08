package cn.ussshenzhou.mobs;

import cn.ussshenzhou.mobs.entity.BlockPretender;
import cn.ussshenzhou.mobs.entity.PlayerPretender;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * @author USS_Shenzhou
 */
@Mod(Mobs.MODID)
public class Mobs {

    public static final String MODID = "mobs";

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<EntityType<BlockPretender>> BLOCK_PRETENDER_ENTITY_TYPE = ENTITY_TYPES.register("block_pretender",
            () -> EntityType.Builder.of(BlockPretender::new, MobCategory.MONSTER)
                    .sized(1, 1)
                    .build("block_pretender")
    );
    public static final RegistryObject<EntityType<PlayerPretender>> PLAYER_PRETENDER_ENTITY_TYPE = ENTITY_TYPES.register("player_pretender",
            () -> EntityType.Builder.of(PlayerPretender::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .build("player_pretender")
    );

    public Mobs() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        ENTITY_TYPES.register(modEventBus);
    }
}
