package mod.gottsch.forge.gmm.core.entity.ai.goal.target;

import mod.gottsch.forge.gmm.core.entity.monster.GMMMonster;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Makes a summoned mob assist its summoner: it targets whatever its owner is fighting.
 * The owner is resolved from {@link GMMMonster#getSummonedOwner()} (player or mob).
 *
 * Generalized from Dungeon Denizens.
 *
 * @author Mark Gottschling on Jan 14, 2024
 */
public class SummonedOwnerTargetGoal extends TargetGoal {
    private final GMMMonster mob;

    public SummonedOwnerTargetGoal(GMMMonster mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = this.mob.getSummonedOwner();
        LivingEntity target = this.mob.getTarget();
        if (owner == null || (target != null && !target.equals(owner))) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        LivingEntity owner = this.mob.getSummonedOwner();
        if (owner instanceof Player player) {
            if (player.getLastHurtByMob() != null) {
                this.mob.setTarget(player.getLastHurtByMob());
            } else if (player.getLastHurtMob() != null) {
                this.mob.setTarget(player.getLastHurtMob());
            }
        } else if (owner instanceof Mob ownerMob) {
            this.mob.setTarget(ownerMob.getTarget());
        }
        super.start();
    }
}
