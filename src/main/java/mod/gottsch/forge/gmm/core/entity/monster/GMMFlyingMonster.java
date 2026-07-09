package mod.gottsch.forge.gmm.core.entity.monster;

import mod.gottsch.forge.gmm.core.entity.ownership.Ownership;
import mod.gottsch.forge.gmm.core.entity.ownership.OwnershipType;
import mod.gottsch.forge.gmm.core.entity.ownership.ThrallOrder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Flying counterpart to {@link GMMMonster}: a {@link FlyingMob} base with the same owner tracking (an
 * owner is any entity that summons/conjures this one). Mirrors GMMMonster's owner implementation
 * because FlyingMob and Monster can't share it via inheritance; the shared logic lives in
 * {@link Ownership}, so only the synced-data accessor and the delegating overrides are duplicated here.
 *
 * @author by Mark Gottschling
 */
public abstract class GMMFlyingMonster extends FlyingMob implements OwnableEntity, IGMMMonster {
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(GMMFlyingMonster.class, EntityDataSerializers.OPTIONAL_UUID);

    // ownership kind + summon lifespan are server-side only (see Ownership); the owner UUID above is synced.
    private OwnershipType ownershipType = OwnershipType.NONE;
    private int remainingLifespan = -1;
    private ThrallOrder thrallOrder = ThrallOrder.FOLLOW;
    @Nullable
    private BlockPos guardPos;
    private final List<UUID> thralls = new ArrayList<>();

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
    protected void customServerAiStep() {
        super.customServerAiStep();
        Ownership.tickLifespan(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        Ownership.save(tag, this);
        Ownership.saveThralls(tag, thralls);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        Ownership.load(tag, this);
        Ownership.loadThralls(tag, thralls);
        // legacy fallback: a pre-UUID "Owner" stored as a player name string.
        if (getOwnerUUID() == null && tag.contains(Ownership.TAG_OWNER)) {
            UUID uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), tag.getString(Ownership.TAG_OWNER));
            if (uuid != null) {
                try {
                    this.setOwnerUUID(uuid);
                } catch (Throwable throwable) {
                }
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

    @Override
    public ThrallOrder getThrallOrder() {
        return thrallOrder;
    }

    @Override
    public void setThrallOrder(ThrallOrder order) {
        this.thrallOrder = order == null ? ThrallOrder.FOLLOW : order;
    }

    @Nullable
    @Override
    public BlockPos getGuardPos() {
        return guardPos;
    }

    @Override
    public void setGuardPos(@Nullable BlockPos pos) {
        this.guardPos = pos;
    }

    @Override
    public List<UUID> getThralls() {
        return thralls;
    }

    /**
     * Resolves the owner (the entity that summoned/conjured this mob) from the stored UUID.
     * Handles both player owners and mob owners (the latter only resolvable server-side).
     *
     * @return the owning LivingEntity, or null if there is no owner or it can't be resolved.
     */
    @Nullable
    public LivingEntity getSummonedOwner() {
        return Ownership.resolveOwner(this);
    }
}
