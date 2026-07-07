package mod.gottsch.forge.gmm.core.entity.monster.zombie;

import mod.gottsch.forge.gmm.core.config.MobConfigHelper;
import mod.gottsch.forge.gmm.core.entity.ai.goal.target.SummonedOwnerTargetGoal;
import mod.gottsch.forge.gmm.core.entity.monster.GMMMonster;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A bloated plague-zombie (D&D ogre zombie / Pathfinder plague zombie). It lumbers slowly and hits
 * hard in melee, but its signature threat is its <em>death</em>: the swollen corpse ruptures into a
 * lingering cloud of poisonous miasma (Poison + Hunger + Nausea) that hangs in the area and fades
 * over a few seconds — so killing it in a doorway or on top of yourself is its own punishment.
 * The cloud damages entities (via Poison) but never terrain. Cloud radius / duration / effect
 * strength and whether it bursts at all are codec-driven ({@code gmm:mob_config}).
 *
 * <p>Undead, so it is immune to its own poison; melee, sun-burning, swamp/sewer/cave dweller.
 *
 * @author Mark Gottschling on 7/3/2026
 */
public class Bloater extends GMMMonster {

    // The death-burst is the whole point; keep the cloud generous but short so it's a real hazard
    // without becoming a permanent no-go zone.
    private static final float DEFAULT_CLOUD_RADIUS = 3.0F;
    private static final int DEFAULT_CLOUD_DURATION = 160;   // ticks the cloud persists (8s)
    private static final int DEFAULT_POISON_DURATION = 100;  // ticks of Poison applied on contact (5s)
    private static final int DEFAULT_POISON_AMPLIFIER = 0;   // Poison I
    // sickly green miasma tint for the cloud particles (readability > realism)
    private static final int CLOUD_COLOR = 0x6C8A2C;

    private boolean hasBurst;

    public Bloater(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new SummonedOwnerTargetGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, playerNotOwner));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                // a swollen ogre-zombie: much tougher and hits harder than a vanilla zombie, but slow
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    private float cloudRadius() {
        return (float) MobConfigHelper.get(this).number("cloudRadius", DEFAULT_CLOUD_RADIUS);
    }

    private int cloudDuration() {
        return (int) MobConfigHelper.get(this).number("cloudDuration", DEFAULT_CLOUD_DURATION);
    }

    private int poisonDuration() {
        return (int) MobConfigHelper.get(this).number("poisonDuration", DEFAULT_POISON_DURATION);
    }

    private int poisonAmplifier() {
        return (int) MobConfigHelper.get(this).number("poisonAmplifier", DEFAULT_POISON_AMPLIFIER);
    }

    private boolean burstsOnDeath() {
        return MobConfigHelper.get(this).flag("burstOnDeath", true);
    }

    private boolean appliesHunger() {
        return MobConfigHelper.get(this).flag("hunger", true);
    }

    private boolean appliesNausea() {
        return MobConfigHelper.get(this).flag("nausea", true);
    }

    /** Rupture into a poison-gas cloud when killed (the whole point). Idempotent. */
    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.burst();
    }

    private void burst() {
        if (this.level().isClientSide || this.hasBurst || !this.burstsOnDeath()) {
            return;
        }
        this.hasBurst = true;

        float radius = this.cloudRadius();
        int duration = this.cloudDuration();

        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(0.25D), this.getZ());
        cloud.setOwner(this);
        cloud.setParticle(ParticleTypes.ENTITY_EFFECT);
        cloud.setFixedColor(CLOUD_COLOR);
        cloud.setRadius(radius);
        cloud.setDuration(duration);
        // shrink to nothing over its lifetime so it visibly dissipates rather than snapping off
        cloud.setRadiusPerTick(-radius / (float) duration);
        cloud.setWaitTime(10);

        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, this.poisonDuration(), this.poisonAmplifier()));
        if (this.appliesHunger()) {
            cloud.addEffect(new MobEffectInstance(MobEffects.HUNGER, this.poisonDuration(), 0));
        }
        if (this.appliesNausea()) {
            cloud.addEffect(new MobEffectInstance(MobEffects.CONFUSION, this.poisonDuration(), 0));
        }
        this.level().addFreshEntity(cloud);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        // immune to its own miasma (and poison generally, as befits the undead)
        if (effectInstance.getEffect() == MobEffects.POISON) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    public void aiStep() {
        if (this.isSunBurnTick()) {
            this.setSecondsOnFire(8);
        }
        // a faint miasma seeps off the bloated corpse so it reads as "plague" while still alive
        if (this.level().isClientSide && this.random.nextInt(10) == 0) {
            this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR,
                    this.getRandomX(0.5D),
                    this.getY() + 0.2D + this.random.nextDouble() * 1.4D,
                    this.getRandomZ(0.5D),
                    0.0D, 0.0D, 0.0D);
        }
        super.aiStep();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.15F, 1.0F);
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ZOMBIE_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return 1.90F;
    }
}
