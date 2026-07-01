package mod.gottsch.forge.gmm.core.entity.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Flying counterpart to {@link GMMMonster}: a {@link FlyingMob} base with the same owner tracking (an
 * owner is any entity that summons/conjures this one). Mirrors GMMMonster's owner implementation
 * because FlyingMob and Monster can't share it via inheritance.
 *
 * @author by Mark Gottschling
 */
public abstract class GMMFlyingMonster extends FlyingMob implements OwnableEntity, IGMMMonster {
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(GMMFlyingMonster.class, EntityDataSerializers.OPTIONAL_UUID);

    protected GMMFlyingMonster(EntityType<? extends FlyingMob> entityType, Level level) {
        super(entityType, level);
    }

    public final Predicate<LivingEntity> playerNotOwner = (entity) -> {
        if (entity instanceof Player) {
            return getOwnerUUID() == null || !getOwnerUUID().equals(entity.getUUID());
        }
        return true;
    };

    @Override
    public void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.getOwnerUUID() != null) {
            // NOTE uses vanilla naming "Owner"
            tag.putUUID("Owner", this.getOwnerUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        UUID uuid;
        if (tag.hasUUID("Owner")) {
            uuid = tag.getUUID("Owner");
        } else {
            String s = tag.getString("Owner");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setOwnerUUID(uuid);
            } catch (Throwable throwable) {
            }
        }
    }

    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse((UUID) null);
    }

    @Override
    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(uuid));
    }

    /**
     * Resolves the owner (the entity that summoned/conjured this mob) from the stored UUID.
     * Handles both player owners and mob owners (the latter only resolvable server-side).
     *
     * @return the owning LivingEntity, or null if there is no owner or it can't be resolved.
     */
    @Nullable
    public LivingEntity getSummonedOwner() {
        UUID uuid = getOwnerUUID();
        if (uuid == null) {
            return null;
        }
        Player player = this.level().getPlayerByUUID(uuid);
        if (player != null) {
            return player;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }
}
