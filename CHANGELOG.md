# Changelog for gottsch's Monster Manual (Forge 1.20.1)

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- ⚓ **Anchoring, for every GMM mob** — a consumer can post any GMM mob to guard a position by
  calling vanilla's own `Mob#restrictTo(BlockPos, int)` on the instance it just placed. An anchored
  mob keeps to its post while idle and never despawns; an unanchored one is untouched, so this is
  opt-in per *instance*, not per mob type. New `Anchor` helper plus four members on `GMMMonster` and
  `GMMFlyingMonster`.

  Vanilla leaves two holes that make `restrictTo` alone insufficient, and both are now closed in one
  place: `Mob#checkDespawn` never consults the restriction, so a posted guardian wandered off the
  despawn cliff like any natural spawn; and the restrict centre/radius are persisted by nothing at
  all (`Entity#saveWithoutId` writes `Glowing`, `Silent`, `NoGravity` and `Invulnerable` — vanilla
  only ever sets a restriction on villagers, whose brain re-derives a home from a bed), so a
  guardian came back from a chunk unload free to wander and free to despawn.

  Anchoring is deliberately **not a leash**. The radius gates despawning and the positions the
  wander goals pick — `WaterAvoidingRandomStrollGoal` runs its candidates through
  `isWithinRestriction`, which is what keeps an idle guardian in its room with no extra goal — and
  it is suspended while the mob is engaged. Left unqualified it does something worse than leash: a
  guardian that chased an intruder out of its room spends the rest of its life trying to wander
  back, mid-fight included. `isAnchorSuspended()` is the overridable hook, defaulting to "has a
  target"; a mob with a dormant/active split of its own overrides it. No goals are installed — a
  consumer wanting a mob to actively walk back to its post adds `MoveTowardsRestrictionGoal`.

### Changed
- ⚓ **Grave Zombie, Animated Armor and Wood Golem now share the one anchor implementation.** All
  three had grown their own copy of the same `checkDespawn` override, the same `isWithinRestriction`
  exemption and the same `HomePosX/Y/Z` + `HomeRadius` save-data block. Behaviour is unchanged and
  the tag names are kept verbatim, so guardians already standing in an existing world stay anchored;
  each mob now only states the part that was ever specific to it — the grave zombie's anchor holds
  until it rises (its active *phase*, not merely having a target) and the armour's until it
  activates, both now expressed as one-line `isAnchorSuspended()` overrides.

### Fixed
- 🧟 **Grave Zombie** — a dormant zombie came back from a chunk unload/reload **visible**, showing as
  a zombie head stuck in the floor rather than as a hidden grave. Being burrowed is two things — AI
  off and invisible — and only the first survived a save: `Mob` persists `NoAi`, but nothing at all
  persists the `Invisible` shared-flag bit (`Entity#saveWithoutId` writes `Glowing`, `Silent`,
  `NoGravity` and `Invulnerable`, and vanilla otherwise only ever gets invisibility from a potion
  effect, which is persisted in its own right). The head showed because `GraveZombieModel` sinks the
  rig 26 model units and deliberately stops short of burying it, on the assumption that a fully
  buried zombie is invisible anyway. `readAdditionalSaveData` now restores the flag alongside the
  phase it already restored, so the state can no longer come back half-applied. Nothing else changes:
  a zombie caught mid-reburrow still finishes sinking and sets the flag itself.
- 🧟 **Grave Zombie** — the buried pose now sinks the rig *fully* under the ground plane
  (`SINK_DEPTH` 26 → 34 model units). The humanoid rig spans model y -8 to 24, so 32 is the exact
  depth that drops the crown of the head to ground level and the rest is margin for the hat and armor
  layers. The old 26 was chosen deliberately, on the reasoning that a buried zombie is invisible and
  the sink only had to read as "emerging" mid-transition — so the invisibility flag was the only
  thing hiding the last ~5 units. It isn't any more. Visible effect while surfacing: the zombie now
  climbs half a block further out of the ground over the same telegraph.

## [1.1.0] - 2026-08-20

### Added
- 🟪 **Black Pudding** — the ooze family's fastest and deadliest member. Where the Gelatinous Cube
  is a slow trap and the Gray Ooze a stationary ambusher, this one actively runs you down: 30 HP,
  7 attack damage, and 0.22 movement speed — the family's fastest by a clear margin (Ochre Jelly's
  0.18 was the previous high), but deliberately still under a vanilla Zombie's 0.23, so it outpaces
  the other oozes without outpacing the player. Walking away from it stays a viable option.
- Two mechanics on top of the stat line, each deliberately bounded so they don't cancel the other's
  counterplay:
  - **Tendril lash** — an adult strikes from `reachBonus` blocks (default 1.0) beyond normal melee
    range, so backpedaling doesn't buy the usual safety margin. Note
    that vanilla's base range for this mob is already 1.96 blocks centre-to-centre, so the bonus
    lands the strike at ~2.96 blocks — about 2.2 blocks of visible air.
    The strike is animated by a **pseudopod** drawn procedurally by the new shared `TendrilRenderer`:
    a tapered tube generated at runtime between the body's front face and the victim, whipping out
    along an arc that bows mid-strike and straightens as it lands, then retracting. Not cuboid
    geometry — a stretched `ModelPart` can only ever be a straight line, and cannot arc at all.
    No wind-up — damage lands first and the strike follows through, the way a zombie's swing *is* its
    hit, rather than telegraphing like the Bodak's death gaze. Crucially it
    fires off a **landed-hit counter, not vanilla swing state**: `MeleeAttackGoal` swings before
    `doHurtTarget` and the cooldown gate rejects roughly half those attempts, so animating off the
    swing would show a strike on blows dealing no damage.
  - **Split on slashing/lightning** — the D&D-canonical trigger, translated to Minecraft as "struck
    by a sword or an axe, or by lightning" — Minecraft has no slashing/blunt damage typing, so the
    trigger reads the attacker's mainhand item against `ItemTags.SWORDS`/`ItemTags.AXES` rather than a
    damage category. Unlike Ochre Jelly, which splits on *any* melee hit, this is narrow on purpose:
    a pickaxe, a shovel, a bare fist, arrows, or fire all kill it without splitting, and that
    asymmetry is the mechanic's entire counterplay.
- Split children are bounded and strictly weaker in kind, not just in numbers: `maxGeneration`
  (default 3) caps the chain, each generation halves max health, and **only a generation-0 adult can
  lash** — so only the adult has the extended reach, and the strike itself is the tell for which one
  out-reaches you. Without that, a corridor of puddings would be an unbroken wall of long-reach
  attackers with no gap to retreat through.
- **`TendrilRenderer`** — shared, library-level geometry for "a limb that reaches far beyond its
  body": a tapered, arcing tube generated between two points, with length, taper and curvature all
  continuous parameters. Endpoints are resolved per frame rather than baked, so a caller can either
  fire and forget (Black Pudding's strike) or track a live, moving target (the planned Roper/Reaper
  grapple-and-reel tentacle). Uses the caller's own `VertexConsumer`, so the tube inherits its
  parent's texture, render type, lighting and hurt-flash for free — deliberately not the beacon-beam
  render type, which is full-bright and translucent and would make a wet physical limb glow.
- `BlackPuddingModel` — a single body cube at vanilla slime's exact dimensions, so the family keeps
  one silhouette. It needs its own model class purely to host the procedural lash; there is no other
  geometry. Decorative cuboids were tried in three passes (four corner tendrils, then trailing slime
  shapes, then a blob on the leading face) and all were cut after in-game review — the mob is
  deliberately a plain black cube distinguished by what it does, not what it grows. No translucent
  outer layer: a black pudding is opaque.

### Changed

- **Orc and Orc Shaman skin retextured.** The skin was a cool steel blue (`#37627A`) painted as a
  smooth gradient — the orc's body panel alone carried 164 unique colours, its head another 136,
  which reads as an airbrushed model rather than a Minecraft mob. Skin is now quantised to four flat
  shades chosen from a fixed lookup table, with a low-amplitude deterministic speckle so large flat
  panels do not read as vinyl. Orc is green (`#2A3822` / `#3B4E2C` / `#4C6438` / `#5C7744`); Orc
  Shaman is ashen grey (`#2E332E` / `#414841` / `#555D55` / `#677067`). Clothing, belt, staff, eyes
  and tusks are untouched — the recolour is hue-masked to skin only.
- The two textures needed different luminance band cut-offs (orc 58/76/90, shaman 150/163/172)
  because the shaman's source blue was much lighter, but both map onto the same four palette values
  so the two mobs read as one species. The shaman is therefore noticeably darker-skinned than it was.

Originals and the generator live in `art/` (`art/reskin_orc.py`, `art/textures/entity/`), outside
`src/main/resources` so none of it ships in the jar. A third unused variant (`orc_jade.png`) is kept
there for a future orc variant.

### Fixed
- **Extended melee reach never actually applied.** Overriding `Mob#getMeleeAttackRangeSqr` looks like
  the hook — other vanilla code paths do consult it — but in 1.20.1 `MeleeAttackGoal` never calls it;
  its own `getAttackReachSqr` recomputes the range inline from `mob.getBbWidth()`, so the override was
  silently ignored. Added `ExtendedReachMeleeAttackGoal`, which overrides the goal's own method and
  takes a supplied bonus so a mob can vary it live or return zero. Black Pudding uses it; the planned
  Roper/Reaper tentacle will need the same.

- **Shrieker and Violet Fungus can no longer be pushed out of place.** Both are rooted plants, but `setNoAi(true)` and `MOVEMENT_SPEED 0` only stop them moving *themselves* — neither touches physics, so entity collision (`EntitySelector.pushableBy` → `Entity.isPushable`) let a player walking into one slide it across the floor. Both now override `isPushable()` to `false`, as vanilla's `ArmorStand` does for the same reason. Deliberately code rather than datapack: a pushable plant is a bug in any pack.
- Shrieker and Violet Fungus now ship `KNOCKBACK_RESISTANCE 1.0`, so a *hit* no longer slides them either — the same visual bug arriving by the other route, and the likelier one since they are monsters and get attacked. Unlike `isPushable` this stays a knob: `GMMMonster#applyConfigAttributes` overwrites it from a `mob_config`'s `knockbackResistance` when a pack sets one.

Both were found from Dungeons2, which grows these two as floor decoration and so is the first
consumer to have a lot of them standing about in one place. Dungeon Denizens has the same mobs and
had the same behaviour.

## [1.0.1] - 2026- 07-17

### Added
- mod logo

### Fixed
- Shield-holding mobs (Skeleton Warrior, Skeleton Champion, Wight) no longer block and attack
  simultaneously. `RaiseShieldGoal` now lowers the shield for the duration of a mainhand attack
  swing and applies the existing block cooldown before the shield can be raised again.

## [1.0.0] - 2026- 07-17

gottsch's Monster Manual (GMM) is a Minecraft Forge library mod for the 1.20.1 "Dungeon Denizens"
family of mods. It provides shared, dependency-free infrastructure for building D&D-flavored
monsters — base entity classes, AI goals, an ownership/thrall system, a spell/projectile framework,
config-driven tuning (`gmm:mob_config`), and shared sounds/particles/tags — plus a growing roster of
ready-to-use monsters (oozes, zombies, undead, beholder-kin, orcs, and more) that consumer mods
register and skin. GMM itself registers no `EntityType`s or items; it's infrastructure other mods
build on top of.
