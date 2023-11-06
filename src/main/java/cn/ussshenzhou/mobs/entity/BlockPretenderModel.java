package cn.ussshenzhou.mobs.entity;
// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import cn.ussshenzhou.mobs.Mobs;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

/**
 * @author USS_Shenzhou
 */
public class BlockPretenderModel<T extends BlockPretenderEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Mobs.MODID, "block_pretender"), "main");

    private final ModelPart group2;
    private final ModelPart group;
    private final ModelPart legs_left;
    private final ModelPart legs_right;
    private final ModelPart shell;

    private final ModelPart root;

    public BlockPretenderModel(ModelPart root) {
        this.group2 = root.getChild("group2");
        this.group = root.getChild("group");
        this.legs_left = root.getChild("legs_left");
        this.legs_right = root.getChild("legs_right");
        this.shell = root.getChild("shell");

        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition group2 = partdefinition.addOrReplaceChild("group2", CubeListBuilder.create(), PartPose.offset(8.0F, 24.0F, -8.0F));

        PartDefinition cube_r1 = group2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(18, 32).addBox(7.6459F, 3.0F, -0.7884F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(32, 35).addBox(7.6459F, 3.0F, -6.6884F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(30, 0).addBox(7.6459F, 5.0F, -6.7884F, 1.0F, 0.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 1).addBox(7.6459F, 3.0F, -6.7884F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(8.6459F, 3.0F, -6.7884F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(36, 7).addBox(7.1459F, -1.0F, 0.2116F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(7.1459F, -1.0F, -6.7884F, 2.0F, 0.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(36, 18).addBox(7.1459F, -1.0F, -6.7884F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(7.1459F, -1.0F, -6.7884F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(14, 27).addBox(9.1459F, -1.0F, -6.7884F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -7.0F, 8.0F, -3.1416F, -0.3927F, 3.1416F));

        PartDefinition group = partdefinition.addOrReplaceChild("group", CubeListBuilder.create(), PartPose.offset(8.0F, 24.0F, -8.0F));

        PartDefinition cube_r2 = group.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 8).addBox(9.0F, -4.0F, -3.8995F, 0.0F, 7.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(20, 19).addBox(9.5F, 1.0F, -2.8995F, 0.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(35, 24).addBox(9.5F, 1.0F, -2.8995F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(14, 32).addBox(9.5F, 2.0F, 6.0005F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(17, 18).addBox(9.5F, 4.0F, -2.8995F, 2.0F, 0.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(20, 22).addBox(11.5F, 1.0F, -2.8995F, 0.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(30, 18).addBox(9.0F, -4.0F, -3.8995F, 3.0F, 4.2F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(10, 18).addBox(9.0F, -4.0F, -3.8995F, 3.0F, 0.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(9.0F, -4.0F, 6.1005F, 3.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(12.0F, -4.0F, -3.8995F, 0.0F, 7.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -7.0F, 8.0F, -3.1416F, 0.7854F, 3.1416F));

        PartDefinition legs_left = partdefinition.addOrReplaceChild("legs_left", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition cube_r3 = legs_left.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(38, 41).addBox(-7.0F, 6.1026F, -8.6081F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 40).addBox(3.0F, 6.1026F, -8.6081F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(35, 18).addBox(-2.0F, -1.5454F, 5.6736F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 2.7489F, 0.0F, 3.1416F));

        PartDefinition cube_r4 = legs_left.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(36, 1).addBox(-7.0F, -1.7683F, -10.4556F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(33, 29).addBox(3.0F, -1.7683F, -10.4556F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(42, 24).addBox(-2.0F, 6.3797F, 7.616F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -2.7489F, 0.0F, -3.1416F));

        PartDefinition legs_right = partdefinition.addOrReplaceChild("legs_right", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition cube_r5 = legs_right.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(32, 35).addBox(-2.0F, -1.7683F, -10.4556F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(26, 40).addBox(3.0F, 6.3797F, 7.616F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 43).addBox(-7.0F, 6.3797F, 7.616F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -2.7489F, 0.0F, -3.1416F));

        PartDefinition cube_r6 = legs_right.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(32, 41).addBox(-2.0F, 6.1026F, -8.6081F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 22).addBox(3.0F, -1.5454F, 5.6736F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(23, 34).addBox(-7.0F, -1.5454F, 5.6736F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 2.7489F, 0.0F, 3.1416F));

        PartDefinition shell = partdefinition.addOrReplaceChild("shell", CubeListBuilder.create(), PartPose.offset(8.0F, 24.0F, -8.0F));

        PartDefinition cube_r7 = shell.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 37).addBox(-3.0F, 0.0F, -9.3137F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -7.0F, 8.0F, -3.1416F, 0.7854F, 3.1416F));

        PartDefinition cube_r8 = shell.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(8, 39).addBox(-0.5475F, 0.0F, 6.6704F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -7.0F, 8.0F, -3.1416F, -0.3927F, 3.1416F));

        PartDefinition cube_r9 = shell.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -3.0F, -6.0F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -7.0F, 8.0F, -3.1416F, 0.0F, 3.1416F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        group2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        group.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        legs_left.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        legs_right.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        shell.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {

    }
}