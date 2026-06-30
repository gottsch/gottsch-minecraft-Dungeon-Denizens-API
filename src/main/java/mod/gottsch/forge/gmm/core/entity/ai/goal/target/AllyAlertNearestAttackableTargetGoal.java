package mod.gottsch.forge.gmm.core.entity.ai.goal.target;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Like vanilla NearestAttackableTargetGoal, but when it acquires a player target it also alerts
 * nearby mobs whose EntityType is in {@code allyTag}. The alert set is data-driven via a tag so the
 * goal can live in the shared library with no reference to consumer mob classes.
 *
 * Generalized from Dungeon Denizens' Headless target goal.
 *
 * @author Mark Gottschling
 */
public class AllyAlertNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private static final int ALERT_RANGE_Y = 10;

    private final TagKey<EntityType<?>> allyTag;

    public AllyAlertNearestAttackableTargetGoal(Mob mob, Class<T> targetType, boolean mustSee, TagKey<EntityType<?>> allyTag) {
        super(mob, targetType, mustSee);
        this.allyTag = allyTag;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
        if (this.targetType == Player.class || this.targetType == ServerPlayer.class) {
            this.alertOthers();
        }
        super.start();
    }

    protected void alertOthers() {
        double distance = this.getFollowDistance();
        AABB aabb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(distance, ALERT_RANGE_Y, distance);
        List<? extends Mob> list = this.mob.level().getEntitiesOfClass(Mob.class, aabb, EntitySelector.NO_SPECTATORS);
        for (Mob otherMob : list) {
            if (this.mob != otherMob && otherMob.getTarget() == null && otherMob.getType().is(this.allyTag)) {
                // alert allies to the target we just acquired (set in start()), not getLastHurtByMob()
                // which is usually null when the target was acquired by sight (fixes a latent DD bug).
                alertOther(otherMob, this.mob.getTarget());
            }
        }
    }

    protected void alertOther(Mob otherMob, LivingEntity target) {
        otherMob.setTarget(target);
    }
}
