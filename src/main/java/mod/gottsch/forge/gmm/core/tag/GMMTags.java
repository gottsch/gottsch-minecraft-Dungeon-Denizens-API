package mod.gottsch.forge.gmm.core.tag;

import mod.gottsch.forge.gmm.core.GMM;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;

/**
 * @author by Mark Gottschling on 11/6/2025
 */
@Mod.EventBusSubscriber(modid = GMM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GMMTags {
    public static class Items {

        public static final TagKey<Item> GHOUL_FOOD = mod(GMM.MOD_ID, "ghoul/food");

        // SkeletonWarrior equipment pools (consumer-populated, one per slot)
        public static final TagKey<Item> SKELETON_WARRIOR_WEAPONS = mod(GMM.MOD_ID, "skeleton_warrior/weapons");
        public static final TagKey<Item> SKELETON_WARRIOR_HELMETS = mod(GMM.MOD_ID, "skeleton_warrior/helmets");
        public static final TagKey<Item> SKELETON_WARRIOR_CHESTPLATES = mod(GMM.MOD_ID, "skeleton_warrior/chestplates");
        public static final TagKey<Item> SKELETON_WARRIOR_LEGGINGS = mod(GMM.MOD_ID, "skeleton_warrior/leggings");
        public static final TagKey<Item> SKELETON_WARRIOR_BOOTS = mod(GMM.MOD_ID, "skeleton_warrior/boots");

        // Orc weapon pool (consumer-populated)
        public static final TagKey<Item> ORC_WEAPONS = mod(GMM.MOD_ID, "orc/weapons");

        // Skeleton Champion weapon pool: the elite leader picks its main-hand weapon from this on spawn.
        // gmm ships diamond + netherite swords as the default (an elite carries elite steel); consumers
        // add their own weapons additively.
        public static final TagKey<Item> SKELETON_CHAMPION_WEAPONS = mod(GMM.MOD_ID, "skeleton_champion/weapons");

        // Equipment immune to acid/corrosion damage (Acid Skeleton, Gelatinous Cube, ...). gmm ships
        // diamond/netherite gear as the default; consumers can add their own corrosion-proof items
        // additively. Shared across mobs rather than mob-scoped since it's a material property, not a
        // per-mob equipment pool.
        public static final TagKey<Item> CORROSION_IMMUNE = mod(GMM.MOD_ID, "corrosion_immune");

        // Wight weapon pool: melee (sword) or ranged (bow) -- see Wight#reassessWeaponGoal. gmm ships
        // a modest vanilla default of each so a Wight is armed standalone; consumers add more.
        public static final TagKey<Item> WIGHT_WEAPONS = mod(GMM.MOD_ID, "wight/weapons");

        // Bodak gaze ward: a helmet-slot item that blocks Death Gaze entirely (see
        // Bodak#isLookingAtMe), mirroring vanilla Enderman's own carved-pumpkin ward. gmm ships carved
        // pumpkin as the default so a Bodak is counterable standalone with zero new items; consumers
        // can add their own dedicated ward item (blindfold/goggles/etc.) additively.
        public static final TagKey<Item> BODAK_GAZE_WARD = mod(GMM.MOD_ID, "bodak/gaze_ward");

        // Shadow spawn weapon pool (consumer-populated)
        public static final TagKey<Item> SHADOW_WEAPONS = mod(GMM.MOD_ID, "shadow/weapons");
        // Shadow-bane weapons: bypass the Shadow's metal resistance and deal bonus damage.
        // BANE = +2 (gold-tier bane), MINOR_BANE = +1. Gold swords are handled intrinsically.
        public static final TagKey<Item> SHADOW_BANE = mod(GMM.MOD_ID, "shadow/bane");
        public static final TagKey<Item> SHADOW_MINOR_BANE = mod(GMM.MOD_ID, "shadow/minor_bane");

        public static TagKey<Item> mod(String domain, String path) {
            return ItemTags.create(new ResourceLocation(domain, path));
        }
    }

    /**
     * Entity-type tags used by gmm's reusable AI goals. gmm owns the tag KEYS but
     * deliberately leaves them empty; CONSUMER mods populate them (e.g. a mod adds
     * its own mobs to HEADLESS_HURT_ALLIES via data/gmm/tags/entity_types/headless/hurt_allies.json)
     * so the shared goals never need to reference consumer mob classes.
     */
    public static class EntityTypes {

        // mobs a Headless alerts when it is hurt
        public static final TagKey<EntityType<?>> HEADLESS_HURT_ALLIES = mod(GMM.MOD_ID, "headless/hurt_allies");
        // mobs a Headless alerts when it acquires a target
        public static final TagKey<EntityType<?>> HEADLESS_TARGET_ALLIES = mod(GMM.MOD_ID, "headless/target_allies");

        // mobs a Skeleton Champion rallies (buffs) while alive — consumers add their skeleton types
        public static final TagKey<EntityType<?>> SKELETON_CHAMPION_RALLY_ALLIES = mod(GMM.MOD_ID, "skeleton_champion/rally_allies");

        // mobs a Beholder can Enthrall (EnthrallGoal). Scoped per-caster rather than shared: a
        // Beholder's "high IQ, psychic powers" flavor lets it dominate stronger mobs than, say, a
        // Shadowlord would be able to (which would get its own gmm:shadowlord/enthrall_candidates tag
        // once/if it ever wires EnthrallGoal). gmm ships a default of common overworld/dungeon
        // hostiles; consumers add their own mobs additively.
        public static final TagKey<EntityType<?>> BEHOLDER_ENTHRALL_CANDIDATES = mod(GMM.MOD_ID, "beholder/enthrall_candidates");

        // A Wight that lands a killing blow may raise a fresh thrall instead of leaving a plain kill
        // (see Wight#tryRaiseThrall) via one of two mechanisms, each its own tag so a consumer can tune
        // them independently: SUMMON_ALLIES spawns a brand-new mob (then stamps it a permanent thrall
        // the same way Ownership#enthrall does); ENTHRALL_CANDIDATES instead dominates an existing
        // nearby live mob. gmm ships a zombie default for both; consumers add their own zombie-family
        // mobs additively.
        public static final TagKey<EntityType<?>> WIGHT_SUMMON_ALLIES = mod(GMM.MOD_ID, "wight/summon_allies");
        public static final TagKey<EntityType<?>> WIGHT_ENTHRALL_CANDIDATES = mod(GMM.MOD_ID, "wight/enthrall_candidates");

        public static TagKey<EntityType<?>> mod(String domain, String path) {
            return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(domain, path));
        }
    }

    /**
     * Damage-type tags. gmm owns the KEYS; the JSON is optional — a missing tag simply matches
     * nothing (so no datagen provider is required to ship a sensible default).
     */
    public static class DamageTypes {

        // Damage types that "kill the bones right" — a true, un-resurrectable death for Bloody Bones.
        // Fire/lava is handled intrinsically in code; drop entries here (e.g. a pack's holy/smite
        // damage type) to add more with no code change.
        public static final TagKey<net.minecraft.world.damagesource.DamageType> BLOODY_BONES_TRUE_KILL =
                mod(GMM.MOD_ID, "bloody_bones/true_kill");

        public static TagKey<net.minecraft.world.damagesource.DamageType> mod(String domain, String path) {
            return TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(domain, path));
        }
    }
}
