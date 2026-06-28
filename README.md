# SoulsWeapons Tweaks

SoulsWeapons Tweaks is a Minecraft Forge 1.20.1 addon mod for Star Fantasy modpacks. It adjusts selected Soulslike Weaponry bosses with clearer attack telegraphs, guard-break mechanics, custom boss UI, safer summon behavior, and additional combat ambience.

This source package is provided for public source reference alongside local modpack development. Build outputs, third-party dependency jars, local test files, and private handoff notes are intentionally excluded from the repository.

## Requirements

- Minecraft: 1.20.1
- Forge: 47.4.10 or compatible 47.x build
- Java: 17
- Soulslike Weaponry: 1.3.1 or newer
- SlashBlade Resharped: 1.9.65 or newer
- GeckoLib Forge: 4.8.4, required by Soulslike Weaponry boss animations
- Projectile Damage: runtime-only compatibility dependency used in the local test pack

## Building

The current Gradle script uses a local `libs/` flatDir for mod jars that are not published through this repository. Place these dependency jars in `libs/` before building:

- `soulslike-weaponry-1.3.1-1.20.1-forge.jar`
- `geckolib-forge-1.20.1-4.8.4.jar`
- `SlashBladeResharped-1.20.1-1.9.65.jar`
- `projectile_damage-forge-3.2.2+1.20.1.jar`

Then build with Gradle using Java 17:

```bat
gradle build
```

The built jar is generated in:

```text
build/libs/
```

The checked-in `gradle.properties` follows the original local workspace style and may contain machine-specific Java paths. Adjust it if building on another machine.

## Current Features

- Draugr Boss: attack warning rings, action-lock cleanup, custom guard-break stun handling, configurable damage reduction, and removal of several frustrating vanilla mechanics.
- Night Shade: clone removal, per-skill telegraphs, a configurable guard-break counter, and instant kill on successful break.
- Night Prowler: phase-based telegraphs, stance meter, summon safety rules, ambience meteors, reaction lightning, and phase-2 reverse purple rain ambience.
- Day Stalker: phase-based telegraphs, stance meter, summon safety rules, custom Inferno/Flames Edge visuals, ambience meteors, and reaction trap patterns.
- Shared VFX: attack warning rings, ground warning circles/rectangles, roar waves, sword explosion frames, guard-break HUD beads, and boss combat ambience helpers.

## License

This project is released under the MIT License. See `LICENSE` for details.

Minecraft, Minecraft Forge, Soulslike Weaponry, SlashBlade Resharped, GeckoLib, and other referenced projects are separate projects owned by their respective authors.

## AI Assistance

This project used AI-assisted coding, debugging, asset processing, and documentation support. The released source and mod builds are reviewed, tested, and maintained by the project author.
