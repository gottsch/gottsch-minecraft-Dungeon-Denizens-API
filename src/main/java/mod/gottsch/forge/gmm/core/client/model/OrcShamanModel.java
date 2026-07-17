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
 * Dedicated Orc Shaman rig -- a Blockbench-authored variant of {@link OrcModel} with a cowl (an
 * inflated outer head cube with a genuine alpha cutout for the face, same idiom as vanilla's hat
 * layer) and a full robe (shoulders + torso + independently-posed front/back flaps, merged into
 * {@code orcBody}). Ported from the Blockbench export at
 * {@code Blockbench/Dungeon Denizens/forge/orc shaman/OrcShaman.bbmodel}; kept the same
 * "extend HumanoidModel, hide the vanilla parts, drive the real geometry by hand" hackery
 * {@link OrcModel} already uses so vanilla's attack/walk animation keeps working.
 * <p>
 * The staff is baked directly into {@code orcRightLowerArm}'s geometry ({@code staffShaft} /
 * {@code staffHead}) rather than an equippable item -- {@code OrcShaman} keeps its main hand empty
 * by design, see {@code OrcShaman#populateDefaultEquipmentSlots}.
 * <p>
 * NOTE: the source .bbmodel also defines {@code leftShoulderPad}/{@code rightShoulderPad} groups,
 * excluded from export on purpose (confirmed with the user) -- this rig deliberately has no shoulder
 * pads, a robed caster doesn't wear an orc's spiked pauldrons. The rig also has no hair part (the
 * cowl covers the head entirely), so unlike {@link OrcModel} this model does not toggle hair
 * visibility off {@link Orc#hasHair()}.
 *
 * @author Mark Gottschling on Jul 10, 2026
 */
public class OrcShamanModel<T extends LivingEntity> extends HumanoidModel<T> implements ArmedModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(GMM.MOD_ID, "orc_shaman"), "main");
	private final ModelPart root;
	private final ModelPart orcHead;
	private final ModelPart orcBody;
	private final ModelPart orcLeftArm;
	private final ModelPart orcRightArm;
	private final ModelPart orcLeftLeg;
	private final ModelPart orcRightLeg;
	private final ModelPart mouth;
	private final ModelPart leftBracer;
	private final ModelPart rightBracer;

	private float leftArmX;
	private float rightArmX;
	private float leftArmY;
	private float rightArmY;

	public OrcShamanModel(ModelPart root) {
		super(root);
		this.root = root;
		this.orcHead = root.getChild("orcHead");
		this.orcBody = root.getChild("orcBody");
		this.orcRightArm = orcBody.getChild("orcRightArm");
		this.orcLeftArm = orcBody.getChild("orcLeftArm");
		this.orcLeftLeg = root.getChild("orcLeftLeg");
		this.orcRightLeg = root.getChild("orcRightLeg");
		mouth = orcHead.getChild("jaw");

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

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		////// hackery: add humanoid parts here because the HumanoidModel.createMesh() is not called. ensure not to actually render them later
		partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, CubeDeformation.NONE), PartPose.offset(0.0F, 0.0F, 0.0F));
		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, CubeDeformation.NONE.extend(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(0.0F, 0.0F, 0.0F));
		partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(-6.5F, 4.0F, 0.0F));
		partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(5.0F, 2.0F, 0.0F));
		partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(-1.9F, 12.0F, 0.0F));
		partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(1.9F, 12.0F, 0.0F));
		///////////////////

		///// orc shaman model parts //////////////
		// orcHead: skull "ears", eye plate, and an inflated cowl shell -- all merged into one cube list,
		// same idiom the old torso used. The cowl carries a genuine alpha cutout around the jaw so the
		// face shows through; DO NOT solid-fill that region when repainting the texture.
		PartDefinition orcHead = partdefinition.addOrReplaceChild("orcHead", CubeListBuilder.create().texOffs(34, 83).addBox(-4.1F, -6.0F, -6.0F, 0.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(34, 83).addBox(4.1F, -6.0F, -6.0F, 0.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(70, 48).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 7.0F, 2.0F, new CubeDeformation(-0.01F))
				.texOffs(57, 0).addBox(-4.0F, -6.0F, -5.5F, 8.0F, 9.0F, 10.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -2.0F, -3.0F));

		PartDefinition jaw = orcHead.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(56, 66).addBox(-4.0F, -0.5F, -3.6F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 1.5F, -1.5F, 0.2618F, 0.0F, 0.0F));
		PartDefinition teeth2_r1 = jaw.addOrReplaceChild("teeth2_r1", CubeListBuilder.create().texOffs(53, 37).addBox(6.0F, -3.0F, 2.8F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -4.0F, -6.2F, 0.0F, 0.0F, 0.7854F));
		PartDefinition teeth1_r1 = jaw.addOrReplaceChild("teeth1_r1", CubeListBuilder.create().texOffs(53, 37).addBox(3.0F, 0.0F, 2.8F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -4.2F, -6.3F, 0.0F, 0.0F, 0.7854F));

		PartDefinition orcBody = partdefinition.addOrReplaceChild("orcBody", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		// robe: shoulders + main torso box, plus front/back flaps posed independently of the torso so
		// they can hang/swing on their own (no shoulder pads -- excluded from export on purpose)
		PartDefinition robe = orcBody.addOrReplaceChild("robe", CubeListBuilder.create().texOffs(0, 29).addBox(-10.0F, -26.0F, -3.0F, 20.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 43).addBox(-5.5F, -26.0F, -3.0F, 11.0F, 17.0F, 6.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition frontRobe = robe.addOrReplaceChild("frontRobe", CubeListBuilder.create().texOffs(0, 43).addBox(-5.5F, -1.0F, 0.0F, 11.0F, 17.0F, 6.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -25.0F, -3.0F, -0.0873F, 0.0F, 0.0F));
		PartDefinition backRobe = robe.addOrReplaceChild("backRobe", CubeListBuilder.create().texOffs(35, 43).addBox(-5.5F, 0.0F, -6.0F, 11.0F, 16.0F, 6.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -25.0F, 3.0F, 0.0873F, 0.0F, 0.0F));

		PartDefinition orcLeftArm = orcBody.addOrReplaceChild("orcLeftArm", CubeListBuilder.create().texOffs(0, 78).addBox(-1.5F, -1.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(7.0F, -23.0F, 0.0F, 0.2182F, 0.0F, -0.0873F));
		PartDefinition orcLeftLowerArm = orcLeftArm.addOrReplaceChild("orcLeftLowerArm", CubeListBuilder.create().texOffs(56, 73).addBox(-1.5F, 0.0F, -2.1F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, -0.4363F, 0.0F, 0.0F));
		PartDefinition orcLeftBracer = orcLeftLowerArm.addOrReplaceChild("orcLeftBracer", CubeListBuilder.create().texOffs(70, 58).addBox(4.5F, -14.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offset(-6.0F, 18.0F, 0.0F));

		PartDefinition orcRightArm = orcBody.addOrReplaceChild("orcRightArm", CubeListBuilder.create().texOffs(17, 78).addBox(-2.5F, -1.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-7.0F, -23.0F, 0.0F, 0.1309F, 0.0F, 0.0436F));
		PartDefinition orcRightLowerArm = orcRightArm.addOrReplaceChild("orcRightLowerArm", CubeListBuilder.create().texOffs(73, 73).addBox(-2.5F, 0.0F, -2.1F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
				// staff, baked into the geometry -- see OrcShaman#populateDefaultEquipmentSlots, no equippable item
				.texOffs(0, 0).addBox(-1.0F, 6.0F, -13.0F, 1.0F, 1.0F, 27.0F, new CubeDeformation(0.0F))
				.texOffs(21, 67).addBox(-2.0F, 5.0F, -14.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, -0.2182F, 0.0F, 0.0F));
		PartDefinition orcRightBracer = orcRightLowerArm.addOrReplaceChild("orcRightBracer", CubeListBuilder.create().texOffs(78, 20).addBox(3.5F, -14.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offset(-6.0F, 18.0F, 0.0F));

		PartDefinition orcLeftLeg = partdefinition.addOrReplaceChild("orcLeftLeg", CubeListBuilder.create().texOffs(57, 20).addBox(-2.0F, 1.0F, -2.5F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 12.0F, 0.0F));
		PartDefinition left_boot = orcLeftLeg.addOrReplaceChild("left_boot", CubeListBuilder.create().texOffs(0, 67).addBox(0.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.2F)), PartPose.offset(-2.5F, 12.0F, 0.0F));
		PartDefinition orcRightLeg = partdefinition.addOrReplaceChild("orcRightLeg", CubeListBuilder.create().texOffs(35, 66).addBox(-3.0F, 1.0F, -2.5F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 12.0F, 0.0F));
		PartDefinition right_boot = orcRightLeg.addOrReplaceChild("right_boot", CubeListBuilder.create().texOffs(70, 37).addBox(-0.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.2F)), PartPose.offset(-2.5F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// hackery: calculate humanoid parts positions
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		// reset humanoid right arm position
		this.rightArm.x = -6.5F;
		this.rightArm.y = 4.0F;

		// reset orc arm positions
		orcRightArm.x = rightArmX;
		orcLeftArm.x = leftArmX;

		// get the orc entity and determine what parts are visible (no hair/shoulder-pad toggles here --
		// this rig has neither, see class javadoc)
		Orc orc = (Orc) entity;
		rightBracer.visible = orc.hasBracers();
		leftBracer.visible = rightBracer.visible;

		// head
		this.orcHead.yRot = netHeadYaw * ((float) Math.PI / 180F);

		if (headPitch < 0) {
			this.orcHead.xRot = Math.max(-15, headPitch) * ((float) Math.PI / 180F);
		} else {
			this.orcHead.xRot = Math.min(35, headPitch) * ((float) Math.PI / 180F);
		}

		/*
		 * legs
		 */
		float radians = 0.6F;
		float walkSpeed = 0.9F; // half speed = 0.5
		this.orcRightLeg.xRot = Mth.cos(limbSwing * walkSpeed) * radians * 1.4F * limbSwingAmount;
		this.orcLeftLeg.xRot = Mth.cos(limbSwing * walkSpeed + (float) Math.PI) * radians * 1.4F * limbSwingAmount;

		/*
		 * arms
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
