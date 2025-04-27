package mod.gottsch.forge.ddenizensapi.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.gottsch.forge.ddenizensapi.core.DDApi;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class RatModel<T extends Entity> extends EntityModel<T> {
	public static final String MODEL_NAME = "rat_model";
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DDApi.MOD_ID, MODEL_NAME), "main");

	private final ModelPart rat;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart leftFrontFoot;
	private final ModelPart leftBackFoot;
	private final ModelPart rightFrontFoot;
	private final ModelPart rightBackFoot;
	private final ModelPart tail;
	private final ModelPart part2;
	private final ModelPart part3;

	public RatModel(ModelPart root) {
		this.rat = root.getChild("rat");
		this.head = this.rat.getChild("head");
		this.body = root.getChild("body");
		this.leftFrontFoot = this.body.getChild("leftFrontFoot");
		this.leftBackFoot = this.body.getChild("leftBackFoot");
		this.rightFrontFoot = this.body.getChild("rightFrontFoot");
		this.rightBackFoot = this.body.getChild("rightBackFoot");
		this.tail = this.body.getChild("tail");
		this.part2 = this.tail.getChild("part2");
		this.part3 = this.part2.getChild("part3");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition rat = partdefinition.addOrReplaceChild("rat", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition head = rat.addOrReplaceChild("head", CubeListBuilder.create().texOffs(-2, -1).addBox(-2.0F, -2.75F, -8.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(2, 2).addBox(0.0F, -3.75F, -6.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(2, 2).addBox(-3.0F, -3.75F, -6.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-1.5F, -2.25F, -8.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(-9, -6).addBox(-3.0F, -4.0F, -5.0F, 5.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition leftFrontFoot = body.addOrReplaceChild("leftFrontFoot", CubeListBuilder.create(), PartPose.offset(1.0F, -0.5F, -3.0F));

		PartDefinition leftFrontFoot_r1 = leftFrontFoot.addOrReplaceChild("leftFrontFoot_r1", CubeListBuilder.create().texOffs(0, -1).addBox(0.0F, -1.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 0.0F, -0.1745F, 0.0F));

		PartDefinition leftBackFoot = body.addOrReplaceChild("leftBackFoot", CubeListBuilder.create(), PartPose.offset(1.0F, -0.5F, 3.0F));

		PartDefinition leftBackFoot_r1 = leftBackFoot.addOrReplaceChild("leftBackFoot_r1", CubeListBuilder.create().texOffs(0, -1).addBox(0.0F, -1.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 0.0F, -0.1745F, 0.0F));

		PartDefinition rightFrontFoot = body.addOrReplaceChild("rightFrontFoot", CubeListBuilder.create(), PartPose.offset(-2.0F, -0.5F, -3.0F));

		PartDefinition rightFrontFoot_r1 = rightFrontFoot.addOrReplaceChild("rightFrontFoot_r1", CubeListBuilder.create().texOffs(0, -1).addBox(0.0F, -1.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.5F, 0.0F, 0.0F, 0.1745F, 0.0F));

		PartDefinition rightBackFoot = body.addOrReplaceChild("rightBackFoot", CubeListBuilder.create(), PartPose.offset(-2.0F, -0.5F, 3.0F));

		PartDefinition rightBackFoot_r1 = rightBackFoot.addOrReplaceChild("rightBackFoot_r1", CubeListBuilder.create().texOffs(0, -1).addBox(0.0F, -1.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.5F, 0.0F, 0.0F, 0.1745F, 0.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, -1).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -2.0F, 2.0F, -0.0436F, 0.0F, 0.0F));

		PartDefinition part2 = tail.addOrReplaceChild("part2", CubeListBuilder.create().texOffs(0, -1).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition part3 = part2.addOrReplaceChild("part3", CubeListBuilder.create().texOffs(0, -1).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 3.0F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		rat.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}