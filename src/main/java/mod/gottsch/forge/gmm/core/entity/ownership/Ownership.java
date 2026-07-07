package mod.gottsch.forge.gmm.core.entity.ownership;

import mod.gottsch.forge.gmm.core.capability.GMMCapabilities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The single access point + shared behavior for entity ownership.
 * <p>
 * <b>The portability boundary.</b> Ownership state can be backed two ways: a GMM mob stores it in its
 * own fields/synced data (it implements {@link IOwnable} directly), while any other entity carries it
 * on a capability/attachment. {@link #of(Entity)} hides that split so every caller works the same for
 * a GMM mob or a vanilla one. When porting to NeoForge, only the non-GMM branch of {@link #of(Entity)}
 * (and {@link GMMCapabilities}) changes -- Forge capability becomes a Data Attachment; everything else
 * here is loader-agnostic.
 *
 * @author Mark Gottschling
 */
public final class Ownership {
    /** NBT keys (shared by GMM-mob save data and the capability provider). "Owner" matches vanilla. */
    public static final String TAG_OWNER = "Owner";
    public static final String TAG_TYPE = "OwnershipType";
    public static final String TAG_LIFESPAN = "SummonLifespan";

    /** No-op view returned for entities that can't be owned (players, non-living), so callers never NPE. */
    private static final IOwnable NULL = new IOwnable() {
        @Override public UUID getOwnerUUID() { return null; }
        @Override public void setOwnerUUID(UUID uuid) { }
        @Override public OwnershipType getOwnershipType() { return OwnershipType.NONE; }
        @Override public void setOwnershipType(OwnershipType type) { }
        @Override public int getRemainingLifespan() { return -1; }
        @Override public void setRemainingLifespan(int ticks) { }
    };

    private Ownership() { }

    /**
     * Resolves the ownership state for any entity: the entity itself if it's a GMM mob, otherwise its
     * ownership capability, otherwise a no-op view.
     */
    public static IOwnable of(Entity entity) {
        if (entity instanceof IOwnable ownable) {
            return ownable;
        }
        return entity.getCapability(GMMCapabilities.OWNERSHIP).orElse(NULL);
    }

    /**
     * Resolves the live owner entity (the summoner/conjurer) from the stored UUID. Handles both player
     * owners and mob owners (the latter only resolvable server-side).
     *
     * @return the owning LivingEntity, or null if there is no owner or it can't be resolved.
     */
    @Nullable
    public static LivingEntity resolveOwner(Entity mob) {
        UUID uuid = of(mob).getOwnerUUID();
        if (uuid == null) {
            return null;
        }
        Level level = mob.level();
        Player player = level.getPlayerByUUID(uuid);
        if (player != null) {
            return player;
        }
        if (level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }

    /** @return true if {@code candidate} is {@code mob}'s owner. */
    public static boolean isOwnedBy(Entity mob, @Nullable Entity candidate) {
        UUID uuid = of(mob).getOwnerUUID();
        return uuid != null && candidate != null && uuid.equals(candidate.getUUID());
    }

    /** Stamps ownership onto a mob. Lifespan is only retained for {@link OwnershipType#SUMMONED}. */
    public static void stampOwnership(Entity mob, @Nullable LivingEntity owner, OwnershipType type, int lifespan) {
        IOwnable ownable = of(mob);
        ownable.setOwnerUUID(owner == null ? null : owner.getUUID());
        ownable.setOwnershipType(type);
        ownable.setRemainingLifespan(type == OwnershipType.SUMMONED ? lifespan : -1);
    }

    /**
     * Server-tick hook for a summoned mob's lifespan: counts down and unsummons at expiry. A negative
     * lifespan means "permanent" and is left alone. No-op for non-SUMMONED ownership.
     */
    public static void tickLifespan(Mob mob) {
        IOwnable ownable = of(mob);
        if (ownable.getOwnershipType() != OwnershipType.SUMMONED) {
            return;
        }
        int remaining = ownable.getRemainingLifespan();
        if (remaining < 0) {
            return;
        }
        if (remaining <= 0) {
            unsummon(mob);
        } else {
            ownable.setRemainingLifespan(remaining - 1);
        }
    }

    /** "Unsummons" a mob: a puff of poof particles + a dispel sound, then removes it (no loot/death). */
    public static void unsummon(Mob mob) {
        if (mob.level() instanceof ServerLevel serverLevel) {
            for (int p = 0; p < 20; p++) {
                double xSpeed = serverLevel.random.nextGaussian() * 0.02D;
                double ySpeed = serverLevel.random.nextGaussian() * 0.02D;
                double zSpeed = serverLevel.random.nextGaussian() * 0.02D;
                serverLevel.sendParticles(ParticleTypes.POOF, mob.getRandomX(0.5D), mob.getRandomY(), mob.getRandomZ(0.5D), 1, xSpeed, ySpeed, zSpeed, 0.15D);
            }
            serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, mob.getSoundSource(), 1.0F, 1.0F);
        }
        mob.discard();
    }

    /** Writes ownership state to {@code tag}. Only the "Owner" UUID (modern form) is written here. */
    public static void save(CompoundTag tag, IOwnable ownable) {
        if (ownable.getOwnerUUID() != null) {
            tag.putUUID(TAG_OWNER, ownable.getOwnerUUID());
        }
        if (ownable.getOwnershipType() != OwnershipType.NONE) {
            tag.putString(TAG_TYPE, ownable.getOwnershipType().name());
        }
        if (ownable.getRemainingLifespan() >= 0) {
            tag.putInt(TAG_LIFESPAN, ownable.getRemainingLifespan());
        }
    }

    /**
     * Reads ownership state from {@code tag}. Only the modern UUID form of "Owner" is handled here; a
     * caller that must support the legacy string-name owner (see {@code OldUsersConverter}) should
     * handle that itself after calling this.
     */
    public static void load(CompoundTag tag, IOwnable ownable) {
        if (tag.hasUUID(TAG_OWNER)) {
            ownable.setOwnerUUID(tag.getUUID(TAG_OWNER));
        }
        ownable.setOwnershipType(OwnershipType.byName(tag.getString(TAG_TYPE)));
        ownable.setRemainingLifespan(tag.contains(TAG_LIFESPAN) ? tag.getInt(TAG_LIFESPAN) : -1);
    }
}
