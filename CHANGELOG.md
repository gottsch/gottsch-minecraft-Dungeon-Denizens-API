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
- `GraveZombie` (`core/entity/monster/zombie/GraveZombie.java`): an ambush zombie that spawns
  **burrowed** (invisible, AI off) and digs up near a player instead of walking in (see the
  `MobIdeasCatalog`'s "Grave Zombie" entry). Runs on the shared vanilla zombie rig — dirt-caked recolor
  (localized dirt splotches/clumps over the intact vanilla texture, the same "recolor patches, not a
  full hue-shift" idiom as `bloody_skeleton.png`), not new geometry — but the two transitions get a
  dedicated `GraveZombieModel` pose: the whole rig (every part moved together as one unit, not via
  independent per-part rotations — that reads as a broken/twisted pose) physically sinks below/rises to
  its standing Y as it buries/surfaces, arms additionally thrust straight up and fan down to the normal
  shamble as it emerges (reversed on the way back down), plus a small uniform positional shudder and a
  steady trickle of dirt particles at its feet — fed by `getRiseAmount(partialTicks)` the same way
  `BloodyBonesModel#collapse` drives its rise. Its phase machine (`PHASE_BURROWED → PHASE_SURFACING → PHASE_ACTIVE →
  PHASE_REBURROWING`) otherwise mirrors `BloodyBones`'s collapse/rise handling: `setNoAi(true)`
  suppresses `customServerAiStep` entirely while inert, so the timer runs in `tick()` instead. While
  dormant it periodically scans for the nearest non-creative/non-spectator player within `detectRange`
  (a creative-mode player is never a valid ambush target — it stays buried rather than surface for
  someone who isn't actually playing) and, once found, teleports **while still invisible** to a clear,
  sturdy-ground spot within `[ambushMinRadius, ambushMaxRadius]` of them, then surfaces over
  `surfaceTicks` (~2.5s default) of visible digging. Losing its target/line-of-sight for
  `loseSightTicks` reburrows over `reburrowTicks` (the same animation in reverse) rather than letting it
  chase openly — this also extinguishes any fire it caught while active in daylight (`clearFire()`),
  and fire damage is excluded from the struck-while-hidden interrupt (`DamageTypeTags.IS_FIRE`), since
  without both a burning zombie would re-interrupt its own reburrow on every fire tick and never
  actually finish sinking. Already within `ambushMaxRadius` of the detected player? It surfaces right
  there instead of relocating. Being struck always surfaces it immediately in place (skipping straight
  to the animation) regardless of the attacker's game mode — same "shouldn't feel unresponsive" rule as
  Gray Ooze/Mimic, and the way to preview the animation as a creative-mode tester. Only ever digs into
  `minecraft:dirt` (never stone/other terrain, gated both in the reposition search and in DD's new
  `SpawnRulesUtil.checkGraveZombieSpawnRules`), which as a side effect makes it a de facto surface mob —
  DD's biome list swapped the original cave biomes for surface ones (`dark_forest`/`forest`/`plains`/
  `swamp`) accordingly. A consumer can anchor a specific instance to a spot with vanilla's own
  `Mob#restrictTo(BlockPos, int)` (saved/restored manually, since vanilla `Mob` doesn't persist it): a
  restricted instance never relocates and never despawns, so a mapmaker can hand-place a graveyard of
  zombies that rise from their own graves.
  Config-driven under `gmm:mob_config` (`ambush` flag, `detectRange`, `ambushMinRadius`,
  `ambushMaxRadius`, `surfaceTicks`, `reburrowTicks`, `loseSightTicks`) — see `docs/CONFIG.md`.
- `Wight` (`core/entity/monster/zombie/Wight.java`): an elite undead that kept its wits — see the
  `MobIdeasCatalog`'s "Wight" entry. Fights like a person rather than a mindless corpse: `WightModel`
  extends the shared `GMMZombieModel` rig but skips its zombie-arms-out lurch (a new overridable
  `animateArms(float)` hook on `GMMZombieModel`, so the default zombie pose is unaffected for every
  other variant), keeping the normal humanoid walk/attack arm swing instead. Carries a real weapon
  drawn from the `gmm:wight/weapons` item tag (ships iron/stone sword + bow; a bow flips it onto
  `VariantPowerRangedBowAttackGoal` via `reassessWeaponGoal()`, the same melee/ranged switch
  `BowSkeleton` uses) plus a dyed-leather chestplate "burial wrap" as the equipped "clothes".
  The flesh recolor script gained a masking pass for this: the vanilla zombie texture actually bakes
  a teal shirt + blue-purple trousers into the same 64x64 skin as the green head/arm flesh (confirmed
  by sampling the real `zombie.png`, not assumed), so a plain whole-image luminance ramp would have
  recolored the "clothes" pixels too. `is_greenish_flesh` now gates both the colour ramp and the
  detail (speck) pass to only green-dominant pixels — the teal/blue clothing pixels pass through with
  their original vanilla colour untouched — generating `textures/entity/wight.png` (pale, bloodless
  flesh — matches typical D&D depictions; the original ashen-grey ramp was corrected after user
  feedback and preserved rather than discarded, see the `ash_zombie` candidate ramp in the script)
  while leaving the shirt/trousers exactly as vanilla painted them.
  Every landed hit applies a new stacking, temporary max-health-reduction status
  (`GMMMobEffects.WITHERED`, gmm's first registered `MobEffect` — infrastructure only, same
  "must be registered to be usable" exception as `GMMSounds`/`GMMParticles`; vanilla's own
  `MobEffectInstance` duration/removal machinery re-clamps health on expiry, no bespoke expiry
  tracking needed) and heals the Wight for a fraction of the damage dealt.
  Alongside combat it runs two independent, continuously-cast abilities — the same shape Beholder
  uses for its own Enthrall + minion-summon kit, each with its own charge time/cooldown and a shared
  `maxThralls` cap: Summon (a new `SummonThrallGoal`, conjures a fresh mob from the
  `gmm:wight/summon_allies` tag near itself, then `Ownership.enthrall`s it) and Enthrall (the existing
  `EnthrallGoal`, dominates an existing nearby live mob from `gmm:wight/enthrall_candidates` directly)
  — both tags ship a `minecraft:zombie` default. Config-driven under `gmm:mob_config` (`wither`,
  `witherDuration`, `maxWitherStacks`, `lifesteal`, `summonChargeTime`, `summonCooldownTime`,
  `summonSpawnRadius`, `enthrallChargeTime`, `enthrallCooldownTime`, `enthrallRange`, `maxThralls`) —
  see `docs/CONFIG.md`. Base `MAX_HEALTH` corrected 30 -> 41 (D&D 5e's Wight stat block is 45 HP;
  scaled via the project's zombie-baseline ratio, D&D 22 HP <-> MC 20 HP ~0.91x — the same conversion
  the Mimic stat block uses — 45 * 20/22 ~ 41, not the un-scaled 30 first shipped). Pushed even paler
  after a second look at the texture.
  Both Summon and Enthrall now visibly telegraph their charge-up instead of just standing there: each
  goal periodically spawns particles around the caster while charging (soul motes for Summon, witch
  motes for Enthrall — Beholder's own `EnthrallGoal` usage gets this too, for free) and flips a new
  `ICastingMob.setCasting(boolean)` on `start()`/`stop()`; `Wight` implements it via a synced
  `DATA_CASTING` boolean, and `WightModel` reads it to swap in a raised, gently-waving channeling arm
  pose (`animateArms`'s signature grew an entity parameter, on `GMMZombieModel`, so a subclass can key
  its pose off synced state — a mechanical change only, every existing zombie-family variant is
  unaffected since none of them override it).

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

- `OrcShaman` (`core/entity/monster/OrcShaman.java`): a caster variant of `Orc` — see the
  `MobIdeasCatalog`'s "Orc Shaman" entry. Extends `Orc` directly and adds a `CastSpellGoal` on top
  (the goal declares no `Goal.Flag`s, so it never contests Orc's other goals for control — it simply
  casts whenever the target is out of melee range and in sight). The caster's spell is consumer-
  supplied via the new `OrcShaman.spellCaster` static hook, the same pattern as
  `Orc.projectileLauncher`; wired to GMM's own `SpikeGrowthSpell`, giving that spell its first real
  caster (previously only test-wired to Beholder, then reverted). Config-driven `spellChargeTime`
  under `gmm:mob_config` (falls back to `CastSpellGoal`'s own 80-tick default). Now has its own
  dedicated rig (`OrcShamanModel`/`OrcShamanRenderer`, `textures/entity/orc_shaman.png`), ported from a
  user-built Blockbench model: a cowl merged into the head (inflated outer layer, vanilla hat-layer
  idiom) and a robe merged into the torso, no shoulder pads. Textured the previously-flat cowl/robe
  placeholder fill with fold/gradient/gold-trim detail and paled the blue-ish skin tone (HSV
  brighten+desaturate); also closed 3 faces that were fully transparent from certain angles (robe's
  sides, both garments' undersides).
  Fights nothing like a regular Orc after user feedback: `Orc` gained an overridable
  `getCombatGoalOverride()` hook so `OrcShaman` can fully replace the melee/throw switch with the
  same `ThrowProjectileGoal` its ranged-rock-thrower sibling already uses for approach/retreat/
  hold-ground positioning, just constructed with a `null` launcher (`ThrowProjectileGoal` now treats
  a `null` launcher as "position only, throw nothing" instead of assuming one is always supplied) —
  so it stands off at range, retreats if crowded, and only melees (bare fists — `populateDefaultEquipmentSlots`
  always empties the main hand now) at true point-blank, and never lobs rocks. Base stats no longer
  match `Orc`: `ATTACK_DAMAGE` 3.25 → 1.5 and `MAX_HEALTH` 25 → 12, a ~0.44x ratio taken from D&D 5e's
  own Orc stat blocks (a quarterstaff-wielding Orc Shaman's melee average is roughly 0.44x a plain
  Orc's greataxe), applied to both stats rather than melee damage alone since this variant is meant
  to be weaker overall, not just in melee.

### Changed

- moved IDenizensMonster, DenizensMonster to from Dungeons Denizens
  - rename IDenizensMonster to IGMMMonster, GMMMonster
- removed MonsterSize enum and properties
