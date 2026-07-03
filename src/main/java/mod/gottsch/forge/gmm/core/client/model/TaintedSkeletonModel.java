package mod.gottsch.forge.gmm.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.gottsch.forge.gmm.core.GMM;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/**
 * Tainted Skeleton model (authored in Blockbench). The body is split into an inflated ribcage
 * ({@code body}) and a {@code lower_body}, with an unstable taint shard fused inside the ribcage;
 * several parts carry small baked rotations for a decayed, twisted look. {@link #setupAnim} layers
 * the standard skeleton walk/look onto those baked poses (it only drives the animated axes, so the
 * decay tilts survive) and makes the shard <em>pulse</em> like a beating heart.
 *
 * @author Mark Gottschling on 7/3/2026
 */
public class TaintedSkeletonModel<T extends Mob> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(GMM.MOD_ID, "tainted_skeleton_model"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart lower_body;
    private final ModelPart shard_1;
    private final ModelPart shard_2;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_leg;
    private final ModelPart left_leg;

    public TaintedSkeletonModel(ModelPart root) {
        this.root = root.getChild("root");
        this.head = this.root.getChild("head");
        this.body = this.root.getChild("body");
        this.lower_body = this.body.getChild("lower_body");
        this.shard_1 = this.body.getChild("shard_1");
        this.shard_2 = this.shard_1.getChild("shard_2");
        this.right_arm = this.root.getChild("right_arm");
        this.left_arm = this.root.getChild("left_arm");
        this.right_leg = this.root.getChild("right_leg");
        this.left_leg = this.root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
        .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, 0.0F, -0.1309F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.4F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition lower_body = body.addOrReplaceChild("lower_body", CubeListBuilder.create().texOffs(17, 30).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition shard_1 = body.addOrReplaceChild("shard_1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition shard_2 = shard_1.addOrReplaceChild("shard_2", CubeListBuilder.create().texOffs(0, 31).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-0.5F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 2.0F, 0.0F, 0.0F, 0.0F, -0.0436F));

        PartDefinition left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.5F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    // baked decay rotations we must preserve while animating (from the PartPose above)
    private static final float HEAD_TILT_Z = -0.1309F;
    private static final float RIGHT_ARM_TILT_Z = -0.0436F;
    private static final float LEFT_ARM_TILT_Z = 0.0873F;

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // head look (keep the baked decay z-tilt)
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.head.zRot = HEAD_TILT_Z;

        // limbs: drive only xRot (the swing axis) so the baked z-tilts on the arms survive
        this.right_arm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
        this.left_arm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        this.right_arm.zRot = RIGHT_ARM_TILT_Z;
        this.left_arm.zRot = LEFT_ARM_TILT_Z;

        this.right_leg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.left_leg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;

        // pulsating taint shard — grows and shrinks like a beating heart
        float pulse = 1.0F + 0.15F * Mth.sin(ageInTicks * 0.3F);
        this.shard_2.xScale = pulse;
        this.shard_2.yScale = pulse;
        this.shard_2.zScale = pulse;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
