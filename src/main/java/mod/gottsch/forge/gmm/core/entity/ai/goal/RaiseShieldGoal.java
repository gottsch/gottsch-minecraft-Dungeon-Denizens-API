package mod.gottsch.forge.gmm.core.entity.ai.goal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ShieldItem;

/**
 * Raises whatever's equipped in {@code EquipmentSlot.OFFHAND} (checked to actually be a
 * {@link ShieldItem} -- this goal never equips anything itself, see each consumer mob's own
 * {@code populateDefaultEquipmentSlots}) whenever a live, visible target is within {@link #range},
 * and lowers it again once the target backs off, breaks line of sight, or {@link #maxBlockTicks} of
 * continuous blocking elapses. A short cooldown after lowering keeps it from instantly re-raising.
 * <p>
 * Deliberately claims no {@link Goal.Flag}s: raising a shield doesn't stop the mob from moving or
 * swinging its mainhand weapon, exactly like a player holding block in one hand while attacking with
 * the other -- this runs <em>concurrently</em> with {@code MeleeAttackGoal}/ranged attack goals, not
 * instead of them.
 * <p>
 * The actual damage reduction isn't anything this goal implements -- calling
 * {@code startUsingItem(OFF_HAND)} alone is enough to make vanilla's own generic
 * {@code LivingEntity#isBlocking()}/{@code isDamageSourceBlocked()} kick in (that logic was never
 * player-locked), including its front-facing-only requirement -- so a mob has to actually be facing
 * its attacker for a raised shield to matter, same as a player.
 *
 * @author Mark Gottschling
 */
public class RaiseShieldGoal extends Goal {

    // hysteresis so a target hovering right at the range boundary doesn't cause rapid up/down flicker
    // once the shield is already raised.
    private static final double CONTINUE_RANGE_MULTIPLIER = 1.3D;

    private final Mob mob;
    private final double range;
    private final int cooldownTicks;
    private final int maxBlockTicks;

    private int blockTicks;
    private long cooldownUntilTick;

    public RaiseShieldGoal(Mob mob, double range, int cooldownTicks, int maxBlockTicks) {
        this.mob = mob;
        this.range = range;
        this.cooldownTicks = cooldownTicks;
        this.maxBlockTicks = maxBlockTicks;
    }

    private boolean holdingShield() {
        return this.mob.getItemBySlot(EquipmentSlot.OFFHAND).getItem() instanceof ShieldItem;
    }

    private LivingEntity liveTarget() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive() ? target : null;
    }

    @Override
    public boolean canUse() {
        if (this.mob.tickCount < this.cooldownUntilTick || !holdingShield()) {
            return false;
        }
        LivingEntity target = liveTarget();
        return target != null
                && this.mob.distanceToSqr(target) <= this.range * this.range
                && this.mob.getSensing().hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        if (!holdingShield() || (this.maxBlockTicks > 0 && this.blockTicks >= this.maxBlockTicks)) {
            return false;
        }
        LivingEntity target = liveTarget();
        double continueRange = this.range * CONTINUE_RANGE_MULTIPLIER;
        return target != null
                && this.mob.distanceToSqr(target) <= continueRange * continueRange
                && this.mob.getSensing().hasLineOfSight(target);
    }

    @Override
    public void start() {
        this.blockTicks = 0;
        this.mob.startUsingItem(InteractionHand.OFF_HAND);
    }

    @Override
    public void tick() {
        this.blockTicks++;
    }

    @Override
    public void stop() {
        this.mob.stopUsingItem();
        this.cooldownUntilTick = this.mob.tickCount + this.cooldownTicks;
    }
}
