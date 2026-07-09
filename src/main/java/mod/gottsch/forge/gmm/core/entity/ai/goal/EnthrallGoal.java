package mod.gottsch.forge.gmm.core.entity.ai.goal;

import mod.gottsch.forge.gmm.core.entity.monster.IGMMMonster;
import mod.gottsch.forge.gmm.core.entity.ownership.Ownership;
import mod.gottsch.forge.gmm.core.entity.ownership.OwnershipType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Enthrall ability: a charge-up-then-execute effect (not a projectile -- nothing is
 * spawned/thrown) cast on the nearest eligible mob within range. Deliberately independent of
 * {@code mob.getTarget()} -- that field is the caster's own combat target (usually a Player, which
 * isn't a valid Enthrall candidate anyway), so this goal runs its own scan and can enthrall a nearby
 * mob (e.g. a zombie) at the same time the caster is fighting a player. Once charged, it converts the
 * target into a permanent {@link OwnershipType#THRALL} via {@link Ownership#enthrall}. Gated by its
 * own cooldown and a {@code maxThralls} cap checked against {@link IGMMMonster#getThralls()}, so the
 * caster (e.g. a Beholder) can only command so many thralls at once.
 * <p>
 * Eligibility is entirely driven by a consumer-supplied {@code TagKey<EntityType<?>>} (e.g.
 * {@code GMMTags.EntityTypes.BEHOLDER_ENTHRALL_CANDIDATES}) rather than a hardcoded Java type check --
 * this is what lets a different caster (e.g. a Shadowlord that can only dominate shadows and lesser
 * mobs) use its own, differently-scoped candidate tag, and lets a consumer mod add its own custom mobs
 * to an existing caster's tag with no code change.
 * <p>
 * Only usable by an {@link IGMMMonster} (needs somewhere to track the resulting thrall list). GMM
 * owns no concrete "Enthrall" spell entry in any weighted spell list -- this is a standalone goal a
 * consumer mob wires up directly (see {@code CastSpellGoal} for GMM's separate projectile-spell path).
 *
 * @author Mark Gottschling
 */
public class EnthrallGoal extends Goal {
    private static final int DEFAULT_CHARGE_TIME = 100;
    private static final double DEFAULT_RANGE = 16.0D;

    private final Mob mob;
    private final IGMMMonster enthraller;
    private final TagKey<EntityType<?>> candidateTag;
    private final int chargeTime;
    private final int cooldownTime;
    private final int maxThralls;
    private final double range;
    private int chargeTimeCount;
    private int cooldownCount;
    @Nullable
    private Mob currentTarget;

    public EnthrallGoal(Mob mob, TagKey<EntityType<?>> candidateTag, int chargeTime, int cooldownTime, int maxThralls) {
        this(mob, candidateTag, chargeTime, cooldownTime, maxThralls, DEFAULT_RANGE);
    }

    public EnthrallGoal(Mob mob, TagKey<EntityType<?>> candidateTag, int chargeTime, int cooldownTime, int maxThralls, double range) {
        this.mob = mob;
        this.enthraller = (IGMMMonster) mob;
        this.candidateTag = candidateTag;
        this.chargeTime = chargeTime;
        this.cooldownTime = cooldownTime;
        this.maxThralls = maxThralls;
        this.range = range;
    }

    public EnthrallGoal(Mob mob, TagKey<EntityType<?>> candidateTag) {
        this(mob, candidateTag, DEFAULT_CHARGE_TIME, 600, 3, DEFAULT_RANGE);
    }

    @Override
    public boolean canUse() {
        if (cooldownCount > 0 || enthraller.getThralls().size() >= maxThralls) {
            return false;
        }
        return findNearestTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return cooldownCount <= 0 && currentTarget != null && isEligible(currentTarget);
    }

    @Override
    public void start() {
        chargeTimeCount = 0;
        currentTarget = findNearestTarget();
    }

    @Override
    public void stop() {
        chargeTimeCount = 0;
        cooldownCount = cooldownTime;
        currentTarget = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (cooldownCount > 0) {
            --cooldownCount;
            return;
        }
        if (currentTarget == null || !isEligible(currentTarget)) {
            chargeTimeCount = 0;
            return;
        }
        mob.getLookControl().setLookAt(currentTarget, 30.0F, 30.0F);
        if (++chargeTimeCount >= chargeTime) {
            Ownership.enthrall(currentTarget, mob);
            if (mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.WITCH, currentTarget.getX(), currentTarget.getY() + currentTarget.getBbHeight() * 0.5D, currentTarget.getZ(),
                        20, 0.3D, 0.3D, 0.3D, 0.0D);
                serverLevel.playSound(null, currentTarget.getX(), currentTarget.getY(), currentTarget.getZ(), SoundEvents.EVOKER_CAST_SPELL, mob.getSoundSource(), 1.0F, 1.0F);
            }
            chargeTimeCount = 0;
            cooldownCount = cooldownTime;
        }
    }

    /**
     * Scans independently of {@code mob.getTarget()} for the nearest eligible candidate -- this is
     * what lets Enthrall run alongside combat against a Player (never itself a valid candidate).
     */
    @Nullable
    private Mob findNearestTarget() {
        AABB searchBox = mob.getBoundingBox().inflate(range);
        List<Mob> candidates = mob.level().getEntitiesOfClass(Mob.class, searchBox, this::isEligible);
        Mob nearest = null;
        double nearestDistSqr = Double.MAX_VALUE;
        for (Mob candidate : candidates) {
            double distSqr = mob.distanceToSqr(candidate);
            if (distSqr < nearestDistSqr) {
                nearestDistSqr = distSqr;
                nearest = candidate;
            }
        }
        return nearest;
    }

    /** @return true if {@code candidate} is tag-eligible, unowned, in-range, visible Enthrall target. */
    private boolean isEligible(Mob candidate) {
        if (candidate == mob || !candidate.isAlive() || !candidate.getType().is(candidateTag)) {
            return false;
        }
        if (Ownership.of(candidate).getOwnershipType() != OwnershipType.NONE) {
            // already owned by someone (or already this owner's thrall) -- not a valid target.
            return false;
        }
        return mob.distanceToSqr(candidate) <= range * range && mob.hasLineOfSight(candidate);
    }
}
