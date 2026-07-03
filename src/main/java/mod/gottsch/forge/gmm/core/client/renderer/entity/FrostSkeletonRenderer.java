package mod.gottsch.forge.gmm.core.client.renderer.entity;

import mod.gottsch.forge.gmm.core.GMM;
import mod.gottsch.forge.gmm.core.client.model.FrostSkeletonModel;
import mod.gottsch.forge.gmm.core.client.model.SkeletonWarriorModel;
import mod.gottsch.forge.gmm.core.entity.monster.skeleton.FrostSkeleton;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the Frost Skeleton with the ice-augmented {@link FrostSkeletonModel} rig (frost recolor
 * texture). Armor layers reuse the shared {@link SkeletonWarriorModel} (vanilla skeleton armor).
 *
 * @author Mark Gottschling on 7/2/2026
 */
public class FrostSkeletonRenderer<T extends FrostSkeleton> extends HumanoidMobRenderer<T, FrostSkeletonModel<T>> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(GMM.MOD_ID, "textures/entity/frost_skeleton.png");

	public FrostSkeletonRenderer(EntityRendererProvider.Context context) {
		super(context, new FrostSkeletonModel<>(context.bakeLayer(FrostSkeletonModel.LAYER_LOCATION)), 0.8F);
		this.addLayer(
				new HumanoidArmorLayer<>(this, new SkeletonWarriorModel(context.bakeLayer(ModelLayers.SKELETON_INNER_ARMOR)),
						new SkeletonWarriorModel(context.bakeLayer(ModelLayers.SKELETON_OUTER_ARMOR)),
						context.getModelManager()));
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return TEXTURE;
	}
}
