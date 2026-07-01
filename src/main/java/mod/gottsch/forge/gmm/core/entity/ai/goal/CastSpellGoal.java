package mod.gottsch.forge.gmm.core.entity.ai.goal;

import mod.gottsch.forge.gottschcore.random.WeightedCollection;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * A reusable "charge up, then cast" spell goal for any {@link Mob}. The goal handles the
 * range/line-of-sight check and the charge-up timer; the consumer supplies a {@link SpellLauncher}
 * that creates and launches whatever spell projectile it likes, so this class has no knowledge of
 * any concrete spell and can live in the shared library.
 * <p>
 * Generalized from Dungeon Denizens' WeightedCastProjectileGoal (multi-spell, weighted) and
 * CastParalysisGoal (its single-spell special case) -- {@link #CastSpellGoal(Mob, int, SpellLauncher)}
 * covers the latter by wrapping a single-entry {@link WeightedCollection}.
 *
 * @author Mark Gottschling on 7/1/2026
 */
public class CastSpellGoal extends Goal {
    private static final int DEFAULT_CHARGE_TIME = 80;

    /**
     * Creates and launches a spell projectile from the given spawn point toward the target.
     * The goal computes the spawn point (in front of the caster, at half-height); the launcher
     * owns the projectile type and computes its own direction/velocity toward the target.
     */
    @FunctionalInterface
    public interface SpellLauncher {
        void cast(Mob caster, LivingEntity target, double x, double y, double z);
    }

    private final Mob mob;
    private final WeightedCollection<Integer, SpellLauncher> spells;
    private final int chargeTime;
    /** Extra "don't cast in melee" buffer beyond the caster's own attack reach; 0 = attack reach only. */
    private final double meleeDistance;
    private int chargeTimeCount;

    public CastSpellGoal(Mob caster, WeightedCollection<Integer, SpellLauncher> spells) {
        this(caster, DEFAULT_CHARGE_TIME, spells);
    }

    public CastSpellGoal(Mob caster, int chargeTime, WeightedCollection<Integer, SpellLauncher> spells) {
        this(caster, chargeTime, 0D, spells);
    }

    public CastSpellGoal(Mob caster, int chargeTime, double meleeDistance, WeightedCollection<Integer, SpellLauncher> spells) {
        this.mob = caster;
        this.spells = spells;
        this.chargeTime = chargeTime;
        this.meleeDistance = meleeDistance;
    }

    public CastSpellGoal(Mob caster, int chargeTime, SpellLauncher singleSpell) {
        this(caster, chargeTime, 0D, singleSpell);
    }

    public CastSpellGoal(Mob caster, int chargeTime, double meleeDistance, SpellLauncher singleSpell) {
        this(caster, chargeTime, meleeDistance, new WeightedCollection<Integer, SpellLauncher>().add(1, singleSpell));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTarget() == null) {
            return false;
        }
        double minDistSqr = Math.max(this.meleeDistance, this.getAttackReachSqr(mob.getTarget()));
        return this.mob.distanceToSqr(mob.getTarget().getX(), mob.getTarget().getY(), mob.getTarget().getZ()) > minDistSqr;
    }

    @Override
    public void start() {
        this.chargeTimeCount = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            if (target.distanceToSqr(this.mob) < 4096.0D && this.mob.hasLineOfSight(target)) {
                ++this.chargeTimeCount;

                if (this.chargeTimeCount >= chargeTime) {
                    Vec3 view = this.mob.getViewVector(1.0F);
                    double x = this.mob.getX() + view.x * 2.0D;
                    double y = this.mob.getY(0.5D);
                    double z = this.mob.getZ() + view.z * 2.0D;

                    SpellLauncher spell = this.spells.next();
                    spell.cast(this.mob, target, x, y, z);
                    this.chargeTimeCount = 0;
                    this.mob.playSound(SoundEvents.SPLASH_POTION_THROW, 0.5F, 0.4F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
                }
            } else if (this.chargeTimeCount > 0) {
                --this.chargeTimeCount;
            }
        }
    }

    protected double getAttackReachSqr(LivingEntity entity) {
        return (double) (this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + entity.getBbWidth());
    }
}
