package cn.ussshenzhou.mobs;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL42C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL46;
import org.lwjgl.opengl.GL46C;

import java.io.IOException;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeBusListener {
    private static final ResourceLocation DARKER_SHADER = new ResourceLocation("shaders/post/darker.json");
    private static PostChain darkerPostShaderChain = null;

    public static void resize() {
        if (darkerPostShaderChain != null) {
            var minecraft = Minecraft.getInstance();
            darkerPostShaderChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
        }
    }

    @SubscribeEvent
    public static void darker(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            var minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.player.isSpectator()) {
                return;
            }
            if (darkerPostShaderChain == null) {
                try {
                    darkerPostShaderChain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), minecraft.getMainRenderTarget(), DARKER_SHADER);
                    darkerPostShaderChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
                } catch (IOException ignored) {
                }
            } else {
                darkerPostShaderChain.process(event.getPartialTick());
                minecraft.getMainRenderTarget().bindWrite(false);
            }
        }
    }
}
