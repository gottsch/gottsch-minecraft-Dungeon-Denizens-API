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
