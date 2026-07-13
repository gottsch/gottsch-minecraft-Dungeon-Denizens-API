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
