package cn.ussshenzhou.mobs.entity;

import cn.ussshenzhou.mobs.Mobs;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.data.ModelData;

/**
 * @author USS_Shenzhou
 */
public class BlockPretenderRenderer extends MobRenderer<BlockPretenderEntity, BlockPretenderModel<BlockPretenderEntity>> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Mobs.MODID, "textures/entity/block_pretender.png");

    public BlockPretenderRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new BlockPretenderModel<>(pContext.bakeLayer(BlockPretenderModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(BlockPretenderEntity pEntity) {
        return TEXTURE;
    }

    @Override
    public void render(BlockPretenderEntity entity, float pEntityYaw, float pPartialTicks, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (entity.getEntityData().get(BlockPretenderEntity.PRETENDING)) {
            poseStack.pushPose();
            poseStack.translate(-0.5, 0, -0.5);
            var blockRenderer = Minecraft.getInstance().getBlockRenderer();
            var level = entity.level();
            var state = level.getBlockState(entity.blockPosition().below());
            var model = blockRenderer.getBlockModel(state);
            for (RenderType renderType : model.getRenderTypes(state, level.random, ModelData.EMPTY)) {
                //blockRenderer.renderBatched(state, pos, level, poseStack, pBuffer.getBuffer(renderType), false, level.random, ModelData.EMPTY, renderType);
                blockRenderer.getModelRenderer().tesselateBlock(level, model, state, entity.blockPosition(), poseStack, pBuffer.getBuffer(renderType), false, level.random, 42L, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
            }
            poseStack.popPose();
        } else {
            super.render(entity, pEntityYaw, pPartialTicks, poseStack, pBuffer, pPackedLight);
        }
    }
}
