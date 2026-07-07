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
