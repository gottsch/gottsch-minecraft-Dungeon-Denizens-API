package mod.gottsch.forge.gmm.core.client.model;

import mod.gottsch.forge.gmm.core.entity.monster.zombie.Bloater;
import net.minecraft.client.model.geom.ModelPart;

/**
 * {@link GMMZombieModel} specialized for {@link Bloater}: once its death-rupture has flung its arms
 * loose (see {@link Bloater#areArmsDetached()}), the model's own arm parts are hidden so the corpse
 * doesn't appear to still have arms attached during its brief death-fall render.
 *
 * @author Mark Gottschling on 7/14/2026
 */
public class BloaterModel<T extends Bloater> extends GMMZombieModel<T> {

    public BloaterModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        boolean armsDetached = entity.areArmsDetached();
        this.leftArm.visible = !armsDetached;
        this.rightArm.visible = !armsDetached;
    }
}
