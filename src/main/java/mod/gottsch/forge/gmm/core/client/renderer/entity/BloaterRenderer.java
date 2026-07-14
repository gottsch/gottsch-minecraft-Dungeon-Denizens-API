package mod.gottsch.forge.gmm.core.client.renderer.entity;

import mod.gottsch.forge.gmm.core.GMM;
import mod.gottsch.forge.gmm.core.client.model.BloaterModel;
import mod.gottsch.forge.gmm.core.entity.monster.zombie.Bloater;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the Bloater on the shared vanilla {@link ModelLayers#ZOMBIE} rig (bloated-green recolor
 * texture, no new layer definition needed) via {@link BloaterModel} — a thin specialization that
 * hides the arm parts once they've been flung loose on death. Armor layers reuse the vanilla zombie
 * armor models for parity (the Bloater spawns unarmored by default, but the layer keeps any
 * picked-up gear rendering correctly).
 *
 * @author Mark Gottschling on 7/3/2026
 */
public class BloaterRenderer<T extends Bloater> extends HumanoidMobRenderer<T, BloaterModel<T>> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(GMM.MOD_ID, "textures/entity/bloater.png");

	public BloaterRenderer(EntityRendererProvider.Context context) {
		super(context, new BloaterModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
		this.addLayer(
				new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
						new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)),
						context.getModelManager()));
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return TEXTURE;
	}
}
