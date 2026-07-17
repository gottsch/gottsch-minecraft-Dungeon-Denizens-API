# Changelog for gottsch's Monster Manual 1.20.1

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - Unreleased

gottsch's Monster Manual (GMM) is a Minecraft Forge library mod for the 1.20.1 "Dungeon Denizens"
family of mods. It provides shared, dependency-free infrastructure for building D&D-flavored
monsters — base entity classes, AI goals, an ownership/thrall system, a spell/projectile framework,
config-driven tuning (`gmm:mob_config`), and shared sounds/particles/tags — plus a growing roster of
ready-to-use monsters (oozes, zombies, undead, beholder-kin, orcs, and more) that consumer mods
register and skin. GMM itself registers no `EntityType`s or items; it's infrastructure other mods
build on top of.

### Added

- **Shield Blocking** — new shared `RaiseShieldGoal` (`core/entity/ai/goal/`) lets any `Mob` raise a
  shield already equipped in its offhand whenever a live, visible target closes to melee range, then
  lower it again once the target backs off or a max-duration cap is hit. Claims no `Goal.Flag`s, so it
  runs concurrently with melee/ranged goals rather than replacing them — a mob can block and swing at
  the same time, same as a player. The actual damage reduction is real, not decorative: vanilla's
  `LivingEntity#isBlocking()`/`isDamageSourceBlocked()` were never player-locked, so simply calling
  `startUsingItem(OFF_HAND)` is enough. Wired to three mobs, each with its own `gmm:<mob>/shields` item
  tag (vanilla shield shipped as the default for all three): **Skeleton Warrior** and **Wight** roll a
  shield at a configurable chance (`shieldProbability`, default 0.35 — a Wight only rolls one if it
  didn't also roll a bow) on top of their existing weapon; **Skeleton Champion** always carries one, no
  roll. All cadence/range/duration knobs (`shieldBlocking`/`shieldBlockRange`/`shieldBlockCooldown`/
  `shieldMaxBlockTicks`) are shared `gmm:mob_config` keys, documented once under "Shield Blocking —
  shared" rather than duplicated per mob. **First in-game screenshot found the shield rendering at the
  wrong angle** — root cause: vanilla's arm-bending "blocking" pose (`HumanoidModel.ArmPose.BLOCK`) is
  only ever assigned by `PlayerRenderer`, never for a generic `Mob`, and none of the three mobs' custom
  model classes route through vanilla's private pose dispatch anyway (some fully override `setupAnim`
  without calling `super`). Fixed with a new `GMMAnimationUtils#poseBlockingArm` (replicates vanilla's
  own formula) called directly from each of `SkeletonWarriorModel`/`SkeletonChampionModel`/`WightModel`
  whenever `entity.isUsingItem() && getUsedItemHand() == OFF_HAND`.
- **Wood Golem** — new Family: Constructs mob, ported from Treasure2's "Witherwood Golem": a bark/root
  construct that guards a fixed post rather than roaming, anchored via vanilla's own `Mob#restrictTo`
  (never despawns while restricted, never leashed back mid-fight, matching the `GraveZombie`/
  `AnimatedArmor` idiom rather than reinventing a synced home-position field). Own dedicated
  `WoodGolemModel` rig, `WoodGolemRenderer`, and ported textures (recolored from Treasure2's dark
  "witherwood" palette to real oak wood colors). An iron-golem-style slam attack (wide random damage
  roll + knockback) with iron golem sounds as a stand-in until a dedicated "wood creak" sound is added.
  Stats (`MAX_HEALTH 50.0`/`ATTACK_DAMAGE 10.0`) are D&D-mapped against a new golem-family baseline
  (anchored to Iron Golem, since the project's usual zombie-based health ratio badly undershoots for
  golems) — deliberately ranked tougher than Flesh Golem but weaker than Clay Golem per MC's own
  material-accessibility logic rather than literal D&D CR order. Wired end-to-end into Dungeon
  Denizens (egg/`/summon` spawnable, never a biome natural spawn).
  **Neutral by default, not hardcoded-hostile** — after the first in-game test showed it attacking the
  player unprompted, targeting was reworked: `attacksMonsters` (flag, default `true`) targets whatever
  matches the new cross-cutting `gmm:category/hostile_monsters` tag (a "protector" mob shouldn't force a
  consumer to enumerate a monster roster), `attacksPlayers` (flag, default `false`) is a separate policy
  toggle. This also fully subsumes the earlier Iron Golem hostility removal — iron golems aren't in the
  default hostile-monsters tag, so golems never fight each other regardless of config. Not yet in-game
  visually re-verified.
- **Bloater** — dedicated Blockbench rig (`BloaterZombieModel`) replacing the earlier shared
  `GMMZombieModel`/`ModelLayers.ZOMBIE` reuse: a genuinely swollen torso via two asymmetric,
  angled `frontBloat`/`backBloat` child cubes (offset + ~12.5° tilt) rather than a parallel inflate
  layer, plus swollen legs. Arms no longer raise into the zombie shamble — the normal humanoid
  walk/attack swing is left in place but dampened for idle sway only (a real attack swing is
  untouched) — and the whole torso now waddles (rolls side-to-side once per step with a downward
  bob at each weight-shift peak). Death rupture flings a couple of new `BloaterArm` shrapnel
  entities loose (a real zombie-arm-shaped, Bloater-textured projectile, not a reused bone shard),
  and the model's own arm parts hide once thrown. Texture is now a flesh-masked recolor (only
  exposed skin takes the sickly-green ramp) with a new swampy-decay shirt/pants recolor
  (previously vanilla teal/violet) — the recolor script's base switched from Husk to vanilla Zombie
  since Husk has no distinctly-hued clothing to mask against.
- **Companion Spawning** — new opt-in hook (`GMMMonster`/`GMMFlyingMonster#getCompanionPool()`) that
  spawns a tag-driven escort alongside a mob's own natural/egg/summon spawn, backed by a new public
  `core/util/CompanionSpawner` utility (also directly callable by a consumer for one-off, hand-picked
  encounters, e.g. a custom structure spawner). Recursion/runaway-safe by construction: every
  companion is finalized as `MobSpawnType.MOB_SUMMONED`, a type the trigger check never re-enters, so
  it's capped at exactly one level deep regardless of tag contents. Wired to Beholder
  (`gmm:beholder/companions`) and Wight (`gmm:wight/companions`).
- **Skeleton Champion** — dedicated Blockbench rig (`SkeletonChampionModel`/`SkeletonChampionRenderer`,
  black-steel plated armor with brass/gold belt-buckle trim) replacing the earlier code-only
  entity/AI, plus `ItemInHandLayer` so its tag-rolled weapon (`gmm:skeleton_champion/weapons`,
  diamond/netherite by default) is actually visible. Renders/collides at 1.2x scale to read as
  physically bigger than the rank-and-file skeletons it rallies, and its baked-in plate now carries a
  flat `ARMOR 15.0`/`ARMOR_TOUGHNESS 0.0` (a full vanilla iron armor set's worth of reduction), since
  it wears no real equippable armor pieces.
- `gmm:burning_skeleton/ignite_immune` entity-type tag — mobs the Burning Skeleton's flame (aura
  pulse, melee bite, death burst) won't ignite, on top of itself/its summoner/scoreboard allies.
  `isAlliedTo()` alone doesn't cover "don't burn fellow undead standing next to you" since
  independently-spawned hostile mobs are never on a team by default.
- **Electric Skeleton** — dedicated Blockbench rig (`ElectricSkeletonModel`/`ElectricSkeletonRenderer`,
  bent/jointed arms) replacing the earlier `SkeletonWarriorModel` reuse, with a forearm "electricity"
  cuff on each arm that spins continuously around Y. Base texture's blue speckle veins recolored to
  electric yellow; the cuffs' UV region (previously blank) painted with a jagged bolt. The head's
  existing `GMMEyesLayer` overlay extended with matching lightning-crack accents. `ELECTRIC_SPARK`'s
  particle sprites redone (were nearly transparent/pale, didn't read as electricity) with a jagged
  bolt silhouette, larger `quadSize`, and a slower alpha fade.
- **Animated Armor** — new construct (`core/entity/monster/construct/AnimatedArmor`): a disguised
  stand of equipped vanilla armor with no wearer, dormant until a player wanders close
  (`ProximityActivateGoal`, new, throttled the same way `GraveZombie`'s own dormant-scan is) or hits
  it. Rendered as a fully transparent `HumanoidModel` plus the standard `HumanoidArmorLayer` — no
  dedicated rig, the equipped armor is the entire visual. Rolls a full 4-slot set from new
  `gmm:animated_armor/{helmets,chestplates,leggings,boots}` tags; the chestplate slot specifically can
  never roll empty, since it's the one piece that makes the arm sleeves (and thus a swinging attack)
  visible. Never natural-spawns, same treatment as the two Mimics. `GatedGoal` extracted out of
  `Mimic` into its own reusable class as part of this (Mimic's own doc had flagged this as the
  intended next step).
- **Animated Weapon** — new construct (`core/entity/monster/construct/AnimatedWeapon`): a masterless
  floating sword/axe with a body-less rig (`AnimatedWeaponModel`, an invisible pivot the equipped
  weapon hangs off via `ItemInHandLayer`) that hovers, faces its target, and attacks with a real
  windup telegraph (`isWindingUp()`, 15 ticks, `ParticleTypes.CRIT` sparks, smoothly blended via a
  partial-tick-interpolated `getWindupProgress`) rather than vanilla's own too-short/non-lengthenable
  swing animation. Unlike Animated Armor this natural-spawns (dark/dungeon biomes).
- **Orc Shaman** — a caster variant of `Orc` (`core/entity/monster/OrcShaman`): stands off at range
  and retreats when crowded rather than closing to melee, via a `getCombatGoalOverride()` hook added to
  `Orc` itself (the same `ThrowProjectileGoal` its rock-throwing sibling uses for positioning, built
  with a `null` launcher so it never actually throws — taught `ThrowProjectileGoal` a `null` launcher
  means "position only," a fully backward-compatible change). Casts **Spike Growth** via `CastSpellGoal`
  (`spellChargeTime`/`spellMinRange` tunable in `gmm:mob_config`, default 120 ticks / 4 blocks — Spike
  Growth is its only attack, so the cast interval is kept short) with no weapon ever equipped
  (`populateDefaultEquipmentSlots` always empties the main hand) — melee fallback is bare fists at a
  deliberately weaker `ATTACK_DAMAGE`/`MAX_HEALTH` than a plain `Orc` (D&D 5e's own two Orc stat blocks
  give a ~0.44x ratio, applied to both stats). Own dedicated rig (`OrcShamanModel`/`OrcShamanRenderer`):
  a cowl merged into the head as an inflated outer shell with a genuine alpha cutout for the face, a
  full robe (shoulders + torso + independently-posed front/back flaps, no shoulder pads — a robed
  caster doesn't wear an orc's spiked pauldrons), and a staff baked directly into the geometry rather
  than an equippable item. Texture recolored from a flat placeholder to a woven-cloth grain/fold-shadow
  treatment matching the pants' wood tones, with the staff painted plain wood.

### Fixed

- `AvoidCrowdGoal` (Skeleton Champion's crowd-spacing goal): a symmetric surround — allies pressing in
  evenly on all sides — made the repulsion vectors cancel to ~zero, which the goal treated as "no
  clear way out" and just waited, retrying the same doomed math every 20 ticks. That's exactly the
  "boxed into a knot" case the goal exists for, so it effectively never fired when most needed. A
  symmetric squeeze now falls back to a random escape bearing instead.
- `SkeletonChampionModel#translateToHand` was missing the rig's `main` wrapper-group offset (this
  Blockbench export wraps everything in a `main` group with its own `PartPose` translate, unlike
  vanilla `HumanoidModel`'s zero-offset root), so the equipped weapon rendered ~1.5 blocks too high —
  floating near the head instead of in the hand.
- Skeleton Champion's rally buff pulsed every 60 ticks (3s), re-triggering the war-cry particle burst
  constantly instead of reading as a periodic rally. Raised to 200 ticks (~10s), with duration raised
  to 240 so allies still stay continuously buffed without the visible spam.
- `GraveZombie`: a hand-placed grave anchored via `restrictTo()` kept trying to wander back to its own
  plot mid-fight, since vanilla's wander goals consult `isWithinRestriction` for candidate positions.
  Now ignores the restriction radius entirely once `PHASE_ACTIVE` (despawn-immunity and the
  rise-in-place behavior are untouched, only movement was affected).

### Changed

- moved IDenizensMonster, DenizensMonster to from Dungeons Denizens
  - rename IDenizensMonster to IGMMMonster, GMMMonster
- removed MonsterSize enum and properties
- `ParalysisSpell` (Beholderkin's "Paralysis" eye ray) now applies a real full-movement root
  (new `gmm:paralyzed` effect) instead of vanilla Slowness, with a short default duration (30
  ticks). Added icon/lang assets for `gmm:paralyzed` and GMM's other two effects
  (`gmm:withered`, `gmm:shrieker_darkness`), which were missing both.
- `GelatinousCube`'s "engulf" now actually holds the target: a new `holdDuration` (default 20
  ticks) of `gmm:paralyzed` runs alongside the existing Slowness/Poison, so the target is
  genuinely stuck for ~1s before Slowness (which outlasts it) takes over as a "wriggling free"
  tail. Previously "engulf" was only a single-tick velocity zero — the target could walk again
  immediately, never actually held despite the catalog describing it that way.
