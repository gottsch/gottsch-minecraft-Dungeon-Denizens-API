package mod.gottsch.forge.gmm.core.capability;

import mod.gottsch.forge.gmm.core.entity.ownership.IOwnable;
import mod.gottsch.forge.gmm.core.entity.ownership.Ownership;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Attaches (and serializes) an {@link OwnershipData} to a non-GMM entity via Forge capabilities.
 * Persistence delegates to {@link Ownership#save}/{@link Ownership#load} so a capability-backed mob
 * and a GMM mob write the exact same NBT shape.
 *
 * @author Mark Gottschling
 */
public class OwnershipProvider implements ICapabilitySerializable<CompoundTag> {
    private final OwnershipData data = new OwnershipData();
    private final LazyOptional<IOwnable> holder = LazyOptional.of(() -> data);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return GMMCapabilities.OWNERSHIP.orEmpty(cap, holder);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        Ownership.save(tag, data);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        Ownership.load(tag, data);
    }

    public void invalidate() {
        holder.invalidate();
    }
}
