# gottsch's Monster Manual (GMM)

**The shared monster library behind gottsch's dungeon mods.**

GMM doesn't add anything to your world by itself. It's a base-class library — a shared collection of mob classes, AI goals, models, renderers, and textures that other mods build their monsters on top of. If you have a mod like **Dungeon Denizens** installed, GMM is the engine running underneath it.

---

## How it works

Vanilla monster code isn't built to be shared across mods. Every dungeon mod usually reinvents its own flying AI, its own spell-casting goals, its own summon logic — even when the mobs behave almost identically. GMM fixes that by pulling the reusable parts out into one place.

GMM registers **zero** entities, items, or blocks of its own. It only supplies:

- Base mob classes (`GMMMonster`, `GMMFlyingMonster`, `WingedHumanoid`, and family-specific bases like `BowSkeleton` and `Beholderkin`)
- Reusable AI goals (ranged/thrown-projectile attacks, spell casting, mob summoning, ally alerts, volant flight & landing)
- Shared models, renderers, and textures
- A datapack-driven config system for tuning spawn gating and mob behavior without touching code

Consuming mods (Dungeon Denizens, and future gottsch dungeon mods) register their own `EntityType`s that point at GMM's classes, and reuse GMM's assets. When two mods need the same monster behavior, they share one implementation instead of maintaining two.

---

## What's inside

GMM currently provides base classes and assets for:

- 🧟 **Ghoul family** — Ghoul, Sewer Ghoul (corpse-eating, self-healing undead)
- 💀 **Skeleton family** — Skeleton Warrior, Iron Skeleton, Magma Skeleton, Winged Skeleton (melee, ranged, and flying archer variants)
- 👁️ **Beholderkin family** — Beholder, Death Tyrant, Gazer, Spectator (floating spellcasters with weighted spell selection and mob summoning)
- 🗿 **Gargoyle family** — Gargoyle, Margoyle (stone fliers with launch/cruise/land combat AI)
- 👹 **Orc** — melee/ranged weapon-swapping raiders with tag-driven equipment
- 🌑 **Shadow family** — Shadow, Shadowlord (metal-resistant undead with anti-metal combat mechanics)
- 🎃 **Headless** — ally-alerting undead
- 🪨 **Boulder** — tamable rolling monster with an owner/sleep system
- 🔥 **Daemon** — fire-spouting, mob-summoning nether threat
- 🐊 **Alligator Gar**, 🐀 **Rat** — smaller ambient/hostile critters

Every family shares its behavior through the same base classes, so a bug fix or improvement in GMM benefits every mod built on it at once.

---

## For modpack makers

Mob spawn gating and many behavior values (timings, probabilities, despawn rules, etc.) are **data-driven**, not hardcoded. Consuming mods ship JSON under `data/<namespace>/gmm/mob_config/<name>.json`, and you can override any of it with your own datapack:

```json
{
  "spawn": { "enabled": true, "minHeight": 0, "maxHeight": 255 },
  "properties": { "rangedProbability": 0.5 },
  "flags": { "someToggle": true }
}
```

Edit the JSON and run `/reload` — changes apply the next time a mob spawns. No recompiling, no mixins. See the **[Mob Config Reference](docs/CONFIG.md)** for every key each mob reads, its default, and what it controls.

You can also customize what GMM's monsters wield, eat, and respond to through its **tag system** — see the **[Tags Reference](docs/TAGS.md)** for the full list (weapon/armor pools, ghoul food, Headless ally alerts) and copy-paste datapack examples.

---

## For developers

GMM exists so dungeon-mob mods don't have to duplicate AI and rendering code. If you're building a mod that wants gottsch-style monsters:

- Depend on GMM and register your own `EntityType`s pointing at its base classes
- Contribute equipment, allies, and summon targets through GMM's tag system (`gmm:`-namespaced entity-type and item tags) instead of hardcoding cross-mod references — see the **[Tags Reference](docs/TAGS.md)**
- Reuse GMM's models/renderers/textures, or extend its base classes for your own variants

GMM never references a consumer mod's classes — all cross-mod coupling flows through tags, datapack config, or a small set of static hook points that consumers wire up (e.g. a mob's projectile launcher or ambient sound).

---

## Compatibility

- ✅ Adds no new content by itself — installing GMM alone changes nothing in your world
- ✅ Required by any mod that depends on it (check that mod's page for the specific dependency)
- ✅ Behavior/spawn tuning is datapack-driven, so modpacks can retune monsters without a code change

---

## Companion mods

GMM is required by:

- **[Dungeon Denizens](https://www.curseforge.com/minecraft/mc-mods/dungeon-denizens)** — the full roster of dungeon monsters listed above, spawning in your world

---

## License

Licensed under the **GNU Lesser General Public License (LGPL)**. Source available on [GitHub](https://github.com/gottsch/gottsch-minecraft-Dungeon-Denizens-API).
