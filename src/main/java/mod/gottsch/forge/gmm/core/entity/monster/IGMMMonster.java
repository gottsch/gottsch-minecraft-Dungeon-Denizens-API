package mod.gottsch.forge.gmm.core.entity.monster;

import mod.gottsch.forge.gmm.core.entity.ownership.IOwnable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * The common contract for all GMM monsters. Extends {@link IOwnable} so every GMM mob carries
 * ownership (owner UUID + type + lifespan) directly on its own fields/synced data -- the "GMM half"
 * of the ownership system; non-GMM mobs get the same contract via a capability (see
 * {@link mod.gottsch.forge.gmm.core.entity.ownership.Ownership}).
 *
 * @author by Mark Gottschling on 11/2/2025
 */
public interface IGMMMonster extends IOwnable {
    // /// NOTE
    // these methods are all from other vanilla interfaces or abstract classes
    // but they are added here to ensure that this singular interface can be used
    // for all GMM monsters. (getOwnerUUID/setOwnerUUID now come from IOwnable.)
    // ///
    public void defineSynchedData();
    public void addAdditionalSaveData(CompoundTag tag);
    public void readAdditionalSaveData(CompoundTag tag);
    // /// END of NOTE

    /**
     * Resolves the owner (the entity that summoned/conjured this mob) from the stored UUID.
     */
    @Nullable
    LivingEntity getSummonedOwner();

    /**
     * Whether a mob summoned via {@code SummonGoal} should have its owner set to the summoner.
     * Most GMM mobs are never chance-summoned by another mob, so this defaults to false.
     */
    default boolean canSummonedHaveOwner() {
        return false;
    }
}
