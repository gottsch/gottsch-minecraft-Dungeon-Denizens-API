package mod.gottsch.forge.gmm.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.gottsch.forge.gmm.core.GMM;
import mod.gottsch.forge.gmm.core.entity.monster.Orc;

import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Since I don't fully understand how model rendering and layers work (ie can't get the held item to rotate/swing properly with the arm),
 * this model uses some hackery. It extends the HumanoidModel, but hides all the ModelPart elements, except the the right arm.
 * The right arm is adjusted slightly to be in the proper position. All animation code is run so all values for the arm can be calculated correctly.
 *
 * @author Mark Gottschling on Apr 28, 2022
 *
 * @param <T>
 */
public class OrcModel<T extends LivingEntity> extends HumanoidModel<T> implements ArmedModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(GMM.MOD_ID, "orc"), "main");
	private final ModelPart root;
	private final ModelPart orcHead;
	private final ModelPart orcBody;
	private final ModelPart orcLeftArm;
	private final ModelPart orcRightArm;
	private final ModelPart orcLeftLeg;
	private final ModelPart orcRightLeg;
	private final ModelPart mouth;

	private final ModelPart rightShoulderPad;
	private final ModelPart leftShoulderPad;
	private final ModelPart hair;
	private final ModelPart leftBracer;
	private final ModelPart rightBracer;

	private float leftArmX;
	private float rightArmX;
	private float leftArmY;
	private float rightArmY;

	/**
	 *
	 * @param root
	 */
	public OrcModel(ModelPart root) {
		super(root);
		this.root = root;
		this.orcHead = root.getChild("orcHead");
		this.orcBody = root.getChild("orcBody");
		this.orcRightArm = orcBody.getChild("orcRightArm");
		this.orcLeftArm = orcBody.getChild("orcLeftArm");
		this.orcLeftLeg = root.getChild("orcLeftLeg");
		this.orcRightLeg = root.getChild("orcRightLeg");
		mouth = orcHead.getChild("jaw");
		hair = orcHead.getChild("hair");

		rightShoulderPad = orcBody.getChild("torso").getChild("rightShoulderPad");
		leftShoulderPad = orcBody.getChild("torso").getChild("leftShoulderPad");

		rightBracer = orcRightArm.getChild("orcRightLowerArm").getChild("orcRightBracer");
		leftBracer = orcLeftArm.getChild("orcLeftLowerArm").getChild("orcLeftBracer");

		// save orc arm original positions
		rightArmX = orcRightArm.x;
		leftArmX = orcLeftArm.x;
		rightArmY = orcRightArm.y;
		leftArmY = orcLeftArm.y;

		// hackery: hide humanoid parts
		head.visible = false;
		hat.visible = false;
		body.visible = false;
		rightArm.visible = false;
		leftArm.visible = false;
		rightLeg.visible = false;
		leftLeg.visible = false;
	}

	/**
	 *
	 * @return
	 */
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// TODO fix this - it is not needed - see SkeletonModel
		// MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		// the above creates the mesh from the humanoid.
		// a problem arises that the parts are all based from the root, and not in bones/sub-parts
		// should be able to work around this, as long as you set the Humanoid properties with the correct
		// path to the named part. So you could Humanoid without any of the original parts
		// (but the arms probably have to be in the same position for the item to place properly).

		////// hackery: add humanoid parts here because the HumanoidModel.createMesh() is not called. ensure not to actually render them later
		partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, CubeDeformation.NONE), PartPose.offset(0.0F, 0.0F, 0.0F));
		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, CubeDeformation.NONE.extend(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(0.0F, 0.0F, 0.0F));
		partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(-6.5F, 4.0F, 0.0F));

		partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(5.0F, 2.0F, 0.0F));
		partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(-1.9F, 12.0F, 0.0F));
		partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(1.9F, 12.0F, 0.0F));
		///////////////////

		/////	orc model parts	//////////////
		PartDefinition orcHead = partdefinition.addOrReplaceChild("orcHead", CubeListBuilder.create().texOffs(27, 21).addBox(-4.0F, -6.0F, -5.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -3.0F));
		PartDefinition ear_r1 = orcHead.addOrReplaceChild("ear_r1", CubeListBuilder.create().texOffs(0, 29).addBox(0.0F, -2.0F, 0.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -2.0F, -1.0F, 0.4363F, -0.2618F, -0.2618F));
		PartDefinition ear_r2 = orcHead.addOrReplaceChild("ear_r2", CubeListBuilder.create().texOffs(29, 11).addBox(-1.0F, -2.0F, 0.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -2.0F, -1.0F, 0.4363F, 0.2618F, 0.2618F));
		PartDefinition hair = orcHead.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(0, 29).addBox(2.0F, -2.5F, -3.5F, 3.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, -5.0F, -0.5F));
		PartDefinition jaw = orcHead.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(17, 56).addBox(-4.0F, -0.5F, -3.5F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.4F)), PartPose.offsetAndRotation(0.0F, 1.5F, -1.5F, 0.2618F, 0.0F, 0.0F));
		PartDefinition teeth4_r1 = jaw.addOrReplaceChild("teeth4_r1", CubeListBuilder.create().texOffs(15, 29).addBox(-0.2F, -1.5F, 4.5F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(15, 29).addBox(8.2F, -1.5F, 4.5F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -4.5F, -6.5F, -0.7854F, 0.0F, 0.0F));
		PartDefinition teeth2_r1 = jaw.addOrReplaceChild("teeth2_r1", CubeListBuilder.create().texOffs(20, 29).addBox(6.0F, -3.0F, 2.8F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(20, 29).addBox(3.0F, 0.0F, 2.8F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -4.5F, -6.5F, 0.0F, 0.0F, 0.7854F));
		PartDefinition orcBody = partdefinition.addOrReplaceChild("orcBody", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition torso = orcBody.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -26.0F, -3.0F, 20.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 11).addBox(-5.5F, -22.0F, -3.0F, 11.0F, 11.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition leftShoulderPad = torso.addOrReplaceChild("leftShoulderPad", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition spike2_r1 = leftShoulderPad.addOrReplaceChild("spike2_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4619F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.9619F, -27.0F, 0.5F, 0.0F, 0.0F, 0.5236F));
		PartDefinition spike1_r1 = leftShoulderPad.addOrReplaceChild("spike1_r1", CubeListBuilder.create().texOffs(0, 11).addBox(-1.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5F, -25.0F, 0.5F, 0.0F, 0.0F, 0.6981F));
		PartDefinition left_should_pad_r1 = leftShoulderPad.addOrReplaceChild("left_should_pad_r1", CubeListBuilder.create().texOffs(35, 11).addBox(0.5F, -1.5F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(4.0F, -26.0F, 0.0F, 0.0F, 0.0F, 0.2618F));
		PartDefinition rightShoulderPad = torso.addOrReplaceChild("rightShoulderPad", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition rightSpike2_r1 = rightShoulderPad.addOrReplaceChild("rightSpike2_r1", CubeListBuilder.create().texOffs(0, 0).addBox(0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, -26.0F, 0.5F, 0.0F, 0.0F, -0.5236F));
		PartDefinition rightSpike1_r1 = rightShoulderPad.addOrReplaceChild("rightSpike1_r1", CubeListBuilder.create().texOffs(0, 11).addBox(0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.5F, -25.0F, 0.5F, 0.0F, 0.0F, -0.6981F));
		PartDefinition right_should_pad_r1 = rightShoulderPad.addOrReplaceChild("right_should_pad_r1", CubeListBuilder.create().texOffs(35, 11).addBox(-6.0F, -1.5F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(-4.5F, -26.0F, 0.0F, 0.0F, 0.0F, -0.2618F));
		PartDefinition orcLeftArm = orcBody.addOrReplaceChild("orcLeftArm", CubeListBuilder.create().texOffs(60, 38).addBox(-1.5F, -1.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(7.0F, -23.0F, 0.0F, 0.2182F, 0.0F, -0.0873F));
		PartDefinition orcLeftLowerArm = orcLeftArm.addOrReplaceChild("orcLeftLowerArm", CubeListBuilder.create().texOffs(59, 52).addBox(-1.5F, 0.0F, -2.1F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, -0.4363F, 0.0F, 0.0F));
		PartDefinition orcLeftBracer = orcLeftLowerArm.addOrReplaceChild("orcLeftBracer", CubeListBuilder.create().texOffs(17, 63).addBox(4.5F, -15.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.2F))
		.texOffs(15, 29).addBox(8.0F, -14.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(15, 29).addBox(6.0F, -14.0F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(15, 29).addBox(6.0F, -14.0F, 1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 18.0F, 0.0F));
		PartDefinition orcRightArm = orcBody.addOrReplaceChild("orcRightArm", CubeListBuilder.create().texOffs(60, 26).addBox(-2.5F, -1.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-7.0F, -23.0F, 0.0F, 0.1309F, 0.0F, 0.0436F));
		PartDefinition orcRightLowerArm = orcRightArm.addOrReplaceChild("orcRightLowerArm", CubeListBuilder.create().texOffs(42, 56).addBox(-2.5F, 0.0F, -2.1F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, -0.2182F, 0.0F, 0.0F));
		PartDefinition orcRightBracer = orcRightLowerArm.addOrReplaceChild("orcRightBracer", CubeListBuilder.create().texOffs(0, 61).addBox(3.5F, -15.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.2F))
		.texOffs(15, 29).addBox(3.0F, -14.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(15, 29).addBox(5.0F, -14.0F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(15, 29).addBox(5.0F, -14.0F, 1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 18.0F, 0.0F));
		PartDefinition orcLeftLeg = partdefinition.addOrReplaceChild("orcLeftLeg", CubeListBuilder.create().texOffs(39, 39).addBox(-2.0F, 1.0F, -2.5F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 12.0F, 0.0F));
		PartDefinition left_boot = orcLeftLeg.addOrReplaceChild("left_boot", CubeListBuilder.create().texOffs(55, 15).addBox(0.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.2F)), PartPose.offset(-2.5F, 12.0F, 0.0F));
		PartDefinition orcRightLeg = partdefinition.addOrReplaceChild("orcRightLeg", CubeListBuilder.create().texOffs(18, 39).addBox(-3.0F, 1.0F, -2.5F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 12.0F, 0.0F));
		PartDefinition right_boot = orcRightLeg.addOrReplaceChild("right_boot", CubeListBuilder.create().texOffs(53, 0).addBox(-0.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.2F)), PartPose.offset(-2.5F, 12.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	//	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// hackery: calculate humanoid parts positions
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		// reset humanoid right arm position
		this.rightArm.x = -6.5F;
		this.rightArm.y = 4.0F;

		// reset orc arm positions
		orcRightArm.x = rightArmX;
		orcLeftArm.x = leftArmX;

		// get the orc entity and determine what parts are visible
		Orc orc = (Orc) entity;
		rightShoulderPad.visible = orc.hasRightShoulderPad();
		leftShoulderPad.visible = orc.hasLeftShoulderPad();
		hair.visible = orc.hasHair();
		rightBracer.visible = orc.hasBracers();
		leftBracer.visible = rightBracer.visible;

		// head
		this.orcHead.yRot = netHeadYaw * ((float)Math.PI / 180F);

		if (headPitch < 0) {
			this.orcHead.xRot = Math.max(-15, headPitch)  * ((float)Math.PI / 180F);
		}
		else {
			this.orcHead.xRot = Math.min(35, headPitch) * ((float)Math.PI / 180F);
		}

		/*
		 *  legs
		 */
		float radians = 0.6F;
		float walkSpeed = 0.9F; // half speed = 0.5
		this.orcRightLeg.xRot = Mth.cos(limbSwing * walkSpeed) * radians  * 1.4F * limbSwingAmount;
		this.orcLeftLeg.xRot = Mth.cos(limbSwing  * walkSpeed + (float)Math.PI) * radians * 1.4F * limbSwingAmount;


		/*
		 *  arms
		 */
		// hackery: set orc arms to that of humanoid arms
		this.orcRightArm.xRot = this.rightArm.xRot;
		this.orcLeftArm.xRot = this.leftArm.xRot;

		// bob the arms
		bobArmPart(this.rightArm, ageInTicks, 1.0F);
		bobArmPart(this.orcRightArm, ageInTicks, 1.0F);
		bobArmPart(this.orcLeftArm, ageInTicks, -1.0F);

		rightArm.y = 4.0F + (Mth.cos(ageInTicks * 0.1F) * 0.5F + 0.05F);
		orcRightArm.y = rightArmY + (Mth.cos(ageInTicks * 0.1F) * 0.5F + 0.05F);
		orcLeftArm.y = leftArmY + (Mth.cos(ageInTicks * 0.1F) * 0.5F + 0.05F);

		// bob mouth
		bobMouthPart(mouth, ageInTicks);
	}

	/**
	 *
	 * @param part
	 * @param age
	 * @param direction
	 */
	public static void bobArmPart(ModelPart part, float age, float direction) {
		part.zRot = direction * (0.08726646F + (Mth.cos(age * 0.15F) * 0.05F + 0.05F));
	}

	public static void bobMouthPart(ModelPart mouth, float age) {
		mouth.xRot = Math.max(0.08726646F, 0.08726646F + Mth.cos(age * 0.07F) * -0.2617994F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		orcHead.render(poseStack, buffer, packedLight, packedOverlay);
		orcBody.render(poseStack, buffer, packedLight, packedOverlay);
		orcLeftLeg.render(poseStack, buffer, packedLight, packedOverlay);
		orcRightLeg.render(poseStack, buffer, packedLight, packedOverlay);
	}

	public ModelPart getAttackArm() {
		return getRightArm();
	}

	public ModelPart getRoot() {
		return root;
	}

	public ModelPart getHead() {
		return orcHead;
	}

	public ModelPart getBody() {
		return orcBody;
	}

	public ModelPart getRightArm() {
		return orcRightArm;
	}

	public ModelPart getLeftArm() {
		return orcLeftArm;
	}
}
