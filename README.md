# Hypervision

Hypervision is a rebranded Baritone fork for Minecraft pathfinding, automation, and world navigation.

The project now uses the `hypervision.*` namespace as its canonical API and runtime root, keeps a legacy `baritone.*` compatibility surface in the repo during transition, and defaults its local data to `minecraft/hypervision` with fallback support for existing `minecraft/baritone` data.

## Identity
- Brand: Hypervision
- Visual direction: crimson primary with slate-gray secondary accents
- Canonical package root: `hypervision.*`
- Supported packaging targets in this branch: common, Fabric, Forge, and Tweaker

## Quick Start
- Open chat and use the default prefix `#`
- Try `#goto 1000 500`
- Try `#mine diamond_ore`
- Try `#stop`
- Run `#help` for clickable help and command discovery

## Highlights
- Long-distance segmented A* pathfinding
- Chunk caching for far travel and revisit-heavy routes
- Mining, building, following, farming, and waypoint workflows
- Selection rendering, path overlays, and toast/chat feedback in the Hypervision palette
- Fabric and Forge module support in the same repo, plus the legacy Tweaker path

## Docs
- [Features](FEATURES.md)
- [Setup](SETUP.md)
- [Usage](USAGE.md)

## Project Notes
- Placeholder project links are used until official Hypervision pages are published.
- The canonical working directory is `minecraft/hypervision`.
- Existing `minecraft/baritone` settings and cache data are still read as a fallback during migration.
- The current Gradle setup is happiest on Java 17 for this branch's verification flow.

## Placeholder Links
- Homepage: <https://example.com/hypervision>
- Repository: <https://example.com/hypervision/repository>
- Issues: <https://example.com/hypervision/issues>
- Community: <https://example.com/hypervision/community>
