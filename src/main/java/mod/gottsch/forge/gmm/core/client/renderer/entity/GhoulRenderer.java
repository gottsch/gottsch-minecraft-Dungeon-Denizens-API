/**
 * 
 */
package mod.gottsch.forge.gmm.core.client.renderer.entity;

import mod.gottsch.forge.gmm.core.GMM;
import mod.gottsch.forge.gmm.core.client.model.GhoulModel;
import mod.gottsch.forge.gmm.core.entity.monster.ghoul.Ghoul;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Mark Gottschling on Apr 1, 2022
 *
 */
public class GhoulRenderer<T extends Ghoul> extends MobRenderer<T, GhoulModel<T>> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(GMM.MOD_ID, "textures/entity/ghoul.png");
	
	/**
	 * 
	 * @param context
	 */
	public GhoulRenderer(EntityRendererProvider.Context context) {
        super(context, new GhoulModel<>(context.bakeLayer(GhoulModel.LAYER_LOCATION)), 0.3F);
    }

     @Override
    public ResourceLocation getTextureLocation(Ghoul entity) {
        return TEXTURE;
    }
}
