package mod.gottsch.forge.gmm.core.client.model;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Mob;

/**
 * Shared vanilla-zombie rig for GMM's zombie/husk-recolor mob family (Bloater and, later, Grave
 * Zombie / Wight / husk-based Mummy) — the zombie-side analog of {@link SkeletonWarriorModel}. A
 * variant reuses this directly: its renderer bakes {@link net.minecraft.client.model.geom.ModelLayers#ZOMBIE}
 * (no new layer definition), adds a {@link net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer},
 * and only supplies its own texture.
 *
 * <p>It exists (rather than reusing vanilla {@link net.minecraft.client.model.ZombieModel}) because
 * that class is generically constrained to {@code Zombie} and so can't parameterize on our GMM
 * entity types. Keeps the signature "arms-out" undead shamble for every variant.
 *
 * @author Mark Gottschling on 7/3/2026
 */
public class GMMZombieModel<T extends Mob> extends HumanoidModel<T> {

    public GMMZombieModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        // a mindless undead always lurches forward, arms out to the front (the shambling-zombie
        // signature). isAggressive=false keeps the arms level/forward (~-80deg); passing true would
        // raise them to the ~-120deg aggro-lunge pose (angled up toward the sky).
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, false, this.attackTime, ageInTicks);
    }
}
