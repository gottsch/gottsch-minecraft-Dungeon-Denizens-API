package mod.gottsch.forge.gmm.core.capability;

import mod.gottsch.forge.gmm.core.entity.ownership.IOwnable;
import mod.gottsch.forge.gmm.core.entity.ownership.OwnershipType;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Plain {@link IOwnable} backing store carried by the ownership capability on non-GMM entities. GMM
 * mobs implement {@link IOwnable} on themselves and never use this.
 *
 * @author Mark Gottschling
 */
public class OwnershipData implements IOwnable {
    @Nullable
    private UUID ownerUUID;
    private OwnershipType ownershipType = OwnershipType.NONE;
    private int remainingLifespan = -1;

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public void setOwnerUUID(@Nullable UUID uuid) {
        this.ownerUUID = uuid;
    }

    @Override
    public OwnershipType getOwnershipType() {
        return ownershipType;
    }

    @Override
    public void setOwnershipType(OwnershipType type) {
        this.ownershipType = type == null ? OwnershipType.NONE : type;
    }

    @Override
    public int getRemainingLifespan() {
        return remainingLifespan;
    }

    @Override
    public void setRemainingLifespan(int ticks) {
        this.remainingLifespan = ticks;
    }
}
