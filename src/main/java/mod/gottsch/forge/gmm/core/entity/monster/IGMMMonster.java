package mod.gottsch.forge.gmm.core.entity.monster;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * @author by Mark Gottschling on 11/2/2025
 */
public interface IGMMMonster {
    // /// NOTE
    // these methods are all from other vanilla interfaces or abstract classes
    // but they are added here to ensure that this singular interface can be used
    // for all GMM monsters
    // ///
    public void defineSynchedData();
    public void addAdditionalSaveData(CompoundTag tag);
    public void readAdditionalSaveData(CompoundTag tag);
    public UUID getOwnerUUID();
    void setOwnerUUID(@Nullable UUID uuid);
    // /// END of NOTE
}
