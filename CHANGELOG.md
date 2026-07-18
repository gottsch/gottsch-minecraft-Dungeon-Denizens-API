# Changelog for gottsch's Monster Manual (Forge 1.20.1)

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026- 07-17

gottsch's Monster Manual (GMM) is a Minecraft Forge library mod for the 1.20.1 "Dungeon Denizens"
family of mods. It provides shared, dependency-free infrastructure for building D&D-flavored
monsters — base entity classes, AI goals, an ownership/thrall system, a spell/projectile framework,
config-driven tuning (`gmm:mob_config`), and shared sounds/particles/tags — plus a growing roster of
ready-to-use monsters (oozes, zombies, undead, beholder-kin, orcs, and more) that consumer mods
register and skin. GMM itself registers no `EntityType`s or items; it's infrastructure other mods
build on top of.
