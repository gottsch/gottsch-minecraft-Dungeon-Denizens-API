# Changelog for gottsch's Monster Manual (Forge 1.20.1)

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2026- 07-17

### Added
- mod logo

### Fixed
- **Shrieker and Violet Fungus can no longer be pushed out of place.** Both are rooted plants, but `setNoAi(true)` and `MOVEMENT_SPEED 0` only stop them moving *themselves* — neither touches physics, so entity collision (`EntitySelector.pushableBy` → `Entity.isPushable`) let a player walking into one slide it across the floor. Both now override `isPushable()` to `false`, as vanilla's `ArmorStand` does for the same reason. Deliberately code rather than datapack: a pushable plant is a bug in any pack.
- Shrieker and Violet Fungus now ship `KNOCKBACK_RESISTANCE 1.0`, so a *hit* no longer slides them either — the same visual bug arriving by the other route, and the likelier one since they are monsters and get attacked. Unlike `isPushable` this stays a knob: `GMMMonster#applyConfigAttributes` overwrites it from a `mob_config`'s `knockbackResistance` when a pack sets one.
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
