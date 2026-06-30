package mod.gottsch.forge.gmm.core.entity.ai.goal.target;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Like vanilla HurtByTargetGoal, but its "alert allies" set is data-driven: any nearby mob
 * whose EntityType is in {@code allyTag} is alerted to the attacker (vanilla's version takes an
 * exclude-list of concrete classes; this takes an include-set via a tag). This lets the goal live
 * in the shared library without referencing any consumer mob class - consumers populate the tag.
 *
 * Generalized from Dungeon Denizens' Headless alert goal.
 *
 * @author Mark Gottschling
 */
public class AllyAlertHurtByTargetGoal extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private static final int ALERT_RANGE_Y = 10;

    private final TagKey<EntityType<?>> allyTag;
    private final Class<?>[] toIgnoreDamage;
    private int timeStamp;

    public AllyAlertHurtByTargetGoal(PathfinderMob mob, TagKey<EntityType<?>> allyTag, Class<?>... toIgnoreDamage) {
        super(mob, true);
        this.allyTag = allyTag;
        this.toIgnoreDamage = toIgnoreDamage;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        int i = this.mob.getLastHurtByMobTimestamp();
        LivingEntity livingentity = this.mob.getLastHurtByMob();
        if (i != this.timeStamp && livingentity != null) {
            if (livingentity.getType() == EntityType.PLAYER && this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                return false;
            } else {
                for (Class<?> oclass : this.toIgnoreDamage) {
                    if (oclass.isAssignableFrom(livingentity.getClass())) {
                        return false;
                    }
                }
                return this.canAttack(livingentity, HURT_BY_TARGETING);
            }
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.timeStamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        this.alertOthers();
        super.start();
    }

    protected void alertOthers() {
        double distance = this.getFollowDistance();
        AABB aabb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(distance, ALERT_RANGE_Y, distance);
        List<? extends Mob> list = this.mob.level().getEntitiesOfClass(Mob.class, aabb, EntitySelector.NO_SPECTATORS);
        for (Mob otherMob : list) {
            if (this.mob != otherMob && otherMob.getTarget() == null && otherMob.getType().is(this.allyTag)) {
                alertOther(otherMob, this.mob.getLastHurtByMob());
            }
        }
    }

    protected void alertOther(Mob otherMob, LivingEntity target) {
        otherMob.setTarget(target);
    }
}
