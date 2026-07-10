package mod.gottsch.forge.gmm.core.client.model;

import mod.gottsch.forge.gmm.core.entity.monster.zombie.Bodak;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The Bodak rig: the shared {@link GMMZombieModel} zombie rig, plus a head-only jerk/twitch overlaid on
 * top of the normal look-tracking pose while a Death Gaze charge is building -- the "jerky and
 * puppet-like, as though guided by an unseen, malevolent force" movement is part of the creature's
 * actual D&amp;D description (along with its permanently gaping "silent scream" mouth, a separate,
 * not-yet-built rig/texture change -- see {@code Bodak}'s class doc), not an invented tell.
 *
 * <p>Deliberately isolated to a single axis (head roll, {@code zRot}) on a single part: vanilla's own
 * look-tracking already drives the head's yaw/pitch, so "eye contact" with whoever it's charging at is
 * preserved, and confining the jerk to one part avoids the "different parts rotating independently
 * looks broken" trap {@code GraveZombieModel}'s first shudder pass hit (see that class's doc) -- there's
 * no risk of a twisted-looking rig when only one part ever gets the extra rotation.
 *
 * <p>Motion is a stepped snap, not a smooth oscillation: the head-roll target flips sign once every
 * {@link Bodak#getJerkPeriodTicks()} and holds there with no interpolation between snaps -- "jerk one
 * way, pause, jerk, pause." Only the angle range ({@link Bodak#getJerkMinAngleDegrees()} /
 * {@link Bodak#getJerkMaxAngleDegrees()}) scales with gaze charge (a mild build in intensity); the
 * period is a fixed, config-tunable cadence, not charge-scaled -- per the user's own correction after
 * the first in-game look (round-2 feedback: a period that sped up toward full charge, paired with a
 * wide angle range, read as "rolling all over the place," not a controlled twitch). All three numbers
 * live in {@code gmm:mob_config} (read via {@code Bodak}, not hardcoded here) so they can be retuned
 * without a code change.
 *
 * <p><b>Round-2 correction:</b> the very first pass used {@code head.zRot +=} inside the
 * charge-building branch only. {@code setupAnim} runs once per <em>render frame</em>, not once per game
 * tick (many times more often at typical framerates), so that accumulated the same angle over and over
 * within a single unchanging snap step -- the head visibly spun away rather than snapping to a fixed
 * pose, and since the reset only ever happened inside the {@code charge > 0} branch, the roll never
 * returned to neutral once charging stopped ("doesn't right itself after I stop looking"). Fixed by
 * always <em>setting</em> {@code head.zRot} to an absolute target every frame (charging or not) instead
 * of incrementing it -- see {@link #setupAnim}.
 *
 * @author Mark Gottschling on 7/10/2026
 */
public class BodakModel<T extends Bodak> extends GMMZombieModel<T> {

    public BodakModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        float charge = entity.getGazeCharge();
        if (charge > 0.0F) {
            float minAngle = (float) Math.toRadians(entity.getJerkMinAngleDegrees());
            float maxAngle = (float) Math.toRadians(entity.getJerkMaxAngleDegrees());
            float angle = Mth.lerp(charge, minAngle, maxAngle);
            int period = Math.max(1, entity.getJerkPeriodTicks());
            long step = (long) (ageInTicks / period);
            this.head.zRot = (step % 2 == 0) ? angle : -angle;
        } else {
            // rights itself the instant charging stops, rather than freezing at whatever angle it last
            // snapped to.
            this.head.zRot = 0.0F;
        }
        // vanilla HumanoidModel#setupAnim already copies hat = head as its last internal step, but that
        // happened inside super.setupAnim() above, before this mutation -- re-copy unconditionally (both
        // branches) so the hat layer never lags a frame stale (same gotcha GraveZombieModel's pose
        // mutations hit).
        this.hat.copyFrom(this.head);
    }
}
