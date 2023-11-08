package cn.ussshenzhou.mobs;

import cn.ussshenzhou.mobs.entity.BlockPretenderModel;
import cn.ussshenzhou.mobs.entity.BlockPretenderRenderer;
import cn.ussshenzhou.mobs.entity.PlayerPretenderModel;
import cn.ussshenzhou.mobs.entity.PlayerPretenderRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModBusListener {

    @SubscribeEvent
    public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Mobs.BLOCK_PRETENDER_ENTITY_TYPE.get(), BlockPretenderRenderer::new);
        event.registerEntityRenderer(Mobs.PLAYER_PRETENDER_ENTITY_TYPE.get(), pContext -> new PlayerPretenderRenderer(pContext, true));
    }

    @SubscribeEvent
    public static void registerLayerDefinition(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BlockPretenderModel.LAYER_LOCATION, BlockPretenderModel::createBodyLayer);
    }
}
