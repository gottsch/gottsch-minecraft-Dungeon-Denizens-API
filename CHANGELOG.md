# Changelog for gottsch's Monster Manual 1.20.1

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-04-11

### Added

- Ownership system with relationship types (Phase 1: `REINFORCEMENT` + `SUMMONED`; `THRALL` reserved):
  - `OwnershipType` enum + `IOwnable` contract (owner UUID, type, remaining lifespan) and an
    `Ownership` facade (`Ownership.of(entity)`) that resolves state for GMM mobs (own fields/synced
    data) and non-GMM mobs alike behind one call site.
  - Any entity can now be owned, not just GMM mob bases: non-GMM/vanilla mobs carry ownership on a
    serialized Forge capability (`GMMCapabilities.OWNERSHIP`), persisted across save/reload. The
    capability + the non-GMM branch of `Ownership.of` are the only loader-specific pieces (a Data
    Attachment swap-in on NeoForge).
  - `SUMMONED` mobs get a lifespan (config `summonLifespan`, default 2400t); on expiry they are
    "unsummoned" — poof particles + dispel sound, no loot/death (`Ownership.tickLifespan`). Spectator
    (the summonable beholder-kin) now expires this way, replacing a dead, never-implemented `lifespan`
    field.
  - `SummonedOwnerTargetGoal` (assist-your-owner) now applies only to `SUMMONED`/`THRALL`; a
    `REINFORCEMENT` targets freely.
  - Refactored the previously duplicated owner code out of `GMMMonster`/`GMMFlyingMonster` into the
    shared `Ownership` helper; the bases keep only their synced-data accessor + delegating overrides.
  - Migrated `Daemon` onto the generic system (`canSummonedHaveOwner()` + `summonLifespan` config,
    default 1200t), removing its bespoke `lifespanCount` field/NBT/countdown. This also fixes a latent
    migration bug where a summoned Daemon got no owner (`SummonGoal` only owns opt-in mobs), so it
    never assisted its summoner and never expired.

- **Ownership Phase 2: `THRALL`.** The owner is always another mob (e.g. a Beholder enthralling a
  zombie) — there is no player-facing enthrall or command path.
  - `ThrallOrder` enum (`FOLLOW`/`STAY`/`GUARD`/`ATTACK`) + `IOwnable` grows a standing order and an
    optional bound `GuardPos`, persisted alongside the rest of ownership state for both GMM mobs and
    the non-GMM capability backing.
  - `Ownership.enthrall(target, owner)` stamps permanent `THRALL` ownership and, the first time a mob
    is enthralled, adds the new Thrall goals (`ThrallFollowOwnerGoal`, `ThrallStayGoal`,
    `ThrallGuardGoal`, `ThrallAttackOrderGoal`) directly to its own `goalSelector`/`targetSelector` —
    works on any `Mob`, GMM's own or not. `Ownership.issueOrder`/`issueGuardOrder`/`issueAttackOrder`
    let an owner's own AI change a thrall's standing order at runtime; `ATTACK` is one-shot and reverts
    to `FOLLOW` once its designated target is gone.
  - `SummonedOwnerTargetGoal`'s "assist your owner" behavior now only applies to a THRALL under the
    `FOLLOW` order — `STAY`/`GUARD`/`ATTACK` have their own dedicated goals and shouldn't have the
    owner's combat target mirrored onto them.
  - `IGMMMonster#getThralls()`: a persisted `List<UUID>` of a mob's own thralls (only meaningful for a
    mob that can actually enthrall). `Ownership.getLiveThralls(owner)` resolves it to live `Mob`
    references, pruning any UUID that no longer resolves (e.g. it died).
  - `Beholder`'s new `EnthrallGoal`: a charge-up-then-execute ability (no projectile) that scans
    independently of `mob.getTarget()` for the nearest unowned eligible mob in range, then converts it
    into a THRALL. Deliberately decoupled from the combat target (which is usually a Player, never a
    valid Enthrall candidate) so Beholder can fight a player and charge Enthrall on a nearby zombie in
    the same moment. Gated by its own cooldown and a `maxThralls` cap (config
    `enthrallChargeTime`/`enthrallCooldownTime`/`maxThralls`, defaults `100`/`600`/`3`). Beholder-only
    for now — Death Tyrant/Gazer/Spectator don't wire it.
  - Eligibility is entirely driven by a new `TagKey<EntityType<?>>` constructor param (no hardcoded
    Java type check), scoped **per-caster** rather than shared across every enthraller — a future
    caster with a different theme (e.g. a Shadowlord that can only dominate shadows and lesser mobs)
    gets its own tag with its own default population instead of sharing one pool. Beholder reads
    `gmm:beholder/enthrall_candidates` (new `GMMTags.EntityTypes.BEHOLDER_ENTHRALL_CANDIDATES`), shipped
    with a default of common hostiles via the new `GMMEntityTypeTagsProvider` (zombie/husk/drowned,
    skeleton/stray, spider/cave spider, creeper, witch, pillager/vindicator, blaze, piglin) — see
    `docs/TAGS.md`. Consumers add their own mobs to the tag additively, no code change required.

- `GelatinousCube` (`core/entity/monster/GelatinousCube.java`): a slow ooze, smaller than a
  full-sized ("Big") vanilla Slime. Unlike `Slime`, it never splits on death and never bounces — it
  has no jump goals or `SlimeMoveControl`, so it glides across the ground on normal pathfinding
  navigation instead. Reuses vanilla's `SlimeModel` for rendering (`GelatinousCubeRenderer`) rather
  than a bespoke model, since `SlimeModel` is generic over any `Entity`; the texture is a hue-shifted
  (amber/gold) recolor of vanilla's `slime.png` so it isn't mistaken for a Slime. Leaves a fading
  ooze-smear trail on the ground while moving (new shared `GMMParticles.SLIME_TRAIL` /
  `SlimeTrailParticle`, tinted per spawn call — reuses vanilla's `spell_0..7` sprite frames rather than
  a new texture). Size is an optional `gmm:mob_config` `properties.size` multiplier (default `1.0`),
  baked into synced entity data once at spawn and applied to both its hitbox (`getDimensions`) and its
  render scale (`GelatinousCubeRenderer`) — see `docs/CONFIG.md`. On a successful hit it "engulfs" the
  target: an instant root (zeroed horizontal velocity), vanilla Slowness, and vanilla Poison standing
  in for an acid DoT — the same vanilla-effect-as-flavor approach as `ParalysisSpell`/`Bloater` — plus
  a conservative, independently-toggleable gear-durability nibble reusing `AcidSkeleton`'s corrosion
  pattern and its (now-shared, see below) corrosion-immune item tag. All config-driven under
  `gmm:mob_config` (`engulf`/`corrodeGear` flags, `slownessDuration`, `slownessAmplifier`,
  `acidDuration`, `acidAmplifier`, `corrosion`). Actual hits are throttled to a config-driven
  `attackCooldown` (default 40 ticks/~2s) — vanilla `MeleeAttackGoal` hardcodes its own 20-tick attempt
  cadence via a private field subclasses can't reach, so an off-cooldown attempt is a no-op in
  `doHurtTarget` instead (invisible here since there's no arm to swing anyway).
- `GMMTags.Items.ACID_SKELETON_CORROSION_IMMUNE` renamed to `CORROSION_IMMUNE`
  (`gmm:acid_skeleton/corrosion_immune` → `gmm:corrosion_immune`): it's a material property (diamond/
  netherite resist corrosion), not a per-mob equipment pool, so it's shared by `AcidSkeleton` and the
  new `GelatinousCube` rather than duplicated. GMM is unreleased, so no migration path is needed.
- `OchreJelly` (`core/entity/monster/OchreJelly.java`): another slow ooze reusing vanilla `SlimeModel`
  (distinct yellow-ochre recolor, `OchreJellyRenderer`), sharing `GelatinousCube`'s non-bouncing ground
  movement and attack-cooldown throttle, but with a different gimmick — deliberately different from
  vanilla `Slime`'s death/size-based split (see the `MobIdeasCatalog`'s "Ochre Jelly" entry). It splits
  *on being struck by a melee weapon* (a direct hit from a living attacker — sword, axe, shovel, fist;
  arrows, explosions, and magic/spells/potions don't count) rather than only on death: each split spawns one new, full-size
  jelly at a fraction of its max health (config `splitHealthFraction`, default 3/4). Fire is its one
  weakness: bonus damage, and never triggers a split. A jelly can only split up to `maxSplits` times
  (default 2) — a jelly spawned *by* a split always starts with zero splits of its own and can never
  split again, so growth is linear rather than exponential (one jelly caps out at `1 + maxSplits`
  total, default: up to 3). Config-driven under `gmm:mob_config` (`split` flag,
  `fireDamageMultiplier`, `maxSplits`, `splitHealthFraction`, `attackCooldown`) — see `docs/CONFIG.md`.
- `GrayOoze` (`core/entity/monster/GrayOoze.java`): the roster's first ambush mob — spawns fully
  disguised as "wet stone" (see the `MobIdeasCatalog`'s "Gray Ooze" entry) via a synced
  `revealProgress` float driving both a squash/texture interpolation in `GrayOozeRenderer` (reuses
  `SlimeModel` like the other oozes — a scale/texture blend rather than `Boulder`'s full model morph,
  since `SlimeModel` has no limbs to fold) and which goals are allowed to run at all (movement/attack
  goals refuse to fire while disguised, mirroring `Boulder`'s dormant/active goal-gating). It reveals
  instantly (not gradually) when hit, or when a player comes within `revealRange`, and re-hides after
  `quietTicks` of no target/no one nearby. Its attack is unique among the ooze family: **zero HP
  damage** — `doHurtTarget` never calls `target.hurt(...)`, only a single heavy, throttled gear-
  corrosion hit (config `corrosion`, default 40 — much larger than Acid Skeleton/Gelatinous Cube's
  incidental nibble, since it's the whole attack here), reusing the shared `CORROSION_IMMUNE` tag. Since
  zero HP damage means no vanilla hurt sound/red-flash, a successful corrosion instead plays a
  stone-break sound and bursts stone-crumble particles at the target, so a hit still reads as "something
  happened" even with no damage numbers. While hidden it's squashed to barely poke above the ground
  (less than a pressure plate), textured with a real crop of vanilla's `stone.png` rather than a
  recolor, and its facing snaps to the nearest cardinal direction (a real block is always axis-aligned).
  Config-driven under `gmm:mob_config` (`camouflage` flag,
  `corrosion`, `attackCooldown`, `revealRange`, `quietTicks`) — see `docs/CONFIG.md`.
- Mimic family (`core/entity/monster/mimic/`): ported from Treasure2's `Mimic`, generalized into GMM's
  dependency-free, single-skin base for consumer mods that don't want a Treasure2 dependency (see the
  `MobIdeasCatalog`'s "Chest Mimic"/"Barrel Mimic" entries; Treasure2 keeps its own richly-skinned
  roster). Reworked from Treasure2's fixed spawn-timer reveal into a true ambush: a Mimic spawns fully
  closed, centered and cardinal-facing in its block (`finalizeSpawn`/`snapToBlockPose` — a real placed
  chest/barrel is always axis-aligned and block-centered) and stays that way indefinitely. Every AI
  goal — including `FloatGoal`/`RandomLookAroundGoal`, not just movement/attack — is wrapped in a
  reusable `GatedGoal` so nothing can run while disguised; even idle head-swivel would rotate the whole
  model (no separate head part) and break the illusion, and an ungated `FloatGoal` could bob it in any
  water at its feet. It reveals the instant a player either hits it (`hurt`) or right-clicks it like the
  container it's disguised as (`mobInteract`) — `activate()` flips `isActive()` immediately (same
  "should never feel unresponsive" call as Gray Ooze's reveal) — but the lid still plays out a ~1s
  opening transition afterward via a synced `openProgress` float, rather than popping straight to fully
  open: Treasure2's original derived its transition from raw time-since-spawn, which only worked because
  it always opened on a fixed schedule, not on an arbitrarily-delayed player trigger. Any consumer can
  assign an individual spawned Mimic its own loot table via `setLootTable(ResourceLocation)`, overriding
  the entity type's normally-registered one — the same idea as Treasure2's per-skin/rarity loot
  assignment, reimplemented without reflection by overriding `Mob.getDefaultLootTable()` (protected,
  overridable) instead of reaching for the `private lootTable` field the way Treasure2's SRG-reflection
  hack did (pinned to a 1.18.2 field name, likely broken on 1.20.1).
  - `VanillaChestMimic`: disguised as a plain vanilla chest. Attributes are the D&D 5e Mimic stat block
    (CR 2: 58 HP, AC 12, half speed 15ft, bite 1d8+3 + acid 1d8) scaled to MC via the zombie-baseline
    ratio documented elsewhere in the catalog (D&D 22 HP ↔ MC 20 HP, ~0.91x): 52 HP, 11 attack damage,
    0.12 movement speed.
  - `BarrelMimic`: same Mimic, disguised as a barrel instead. Shares `VanillaChestMimic`'s exact
    attributes rather than Treasure2's differing per-skin numbers — Treasure2's spread is intentional
    rarity-tier balance (rarer container skin = harder mimic), not arbitrary, but GMM's ports are
    single-skin generics with no rarity axis to key that off yet.
  - Both reuse Treasure2's Blockbench geometry/renderer verbatim (`VanillaChestMimicModel`/
    `BarrelMimicModel`, `*Renderer` + the shared `GMMEyesLayer` for their glowing-eye overlay) and a new
    shared `GMMSounds.MIMIC_AMBIENT`.

### Fixed

- Beholderkin (Beholder / Gazer / Spectator / DeathTyrant) flight & attack fixes:
  - `WeightedChanceSummonGoal`: ground scan now uses `isAir()` (covers cave/void air) instead of
    `== Blocks.AIR`, so underground summons find real ground; and a summoner hovering more than 15
    blocks above terrain now resets its cooldown on abort instead of rescanning every tick and never
    summoning.
  - `BeholderkinBiteGoal`: the bite now requires line of sight, so a kin can no longer bite a target
    through a thin wall.
  - `BeholderkinRandomFloatAroundGoal`: no longer picks float targets inside solid terrain (which
    `MoveControl.canReach` could never reach, leaving the mob hovering stuck); targets are now clamped
    to open air within `[ground+1, ground+maxFloatHeight]`.

### Changed

- moved IDenizensMonster, DenizensMonster to from Dungeons Denizens
  - rename IDenizensMonster to IGMMMonster, GMMMonster
- removed MonsterSize enum and properties
