# Hypervision Features

## Pathfinding
- Segmented A* pathfinding that can keep moving while calculating the next segment.
- Cost-aware routing across walking, jumping, digging, placing, falling, ladders, vines, and parkour moves.
- Backtrack-aware recalculation so active routes can splice into newer, better paths.
- Goal abstractions for blocks, coordinates, radius-based targets, axes, and composites.

## World Awareness
- Chunk caching for long-distance travel and repeated searches.
- Block search helpers for mining and locating known targets.
- World data persistence under `minecraft/hypervision`, with fallback reads from legacy `minecraft/baritone` data.

## Automation
- `goto`, `mine`, `follow`, `farm`, `build`, `explore`, `surface`, `tunnel`, and waypoint workflows.
- Elytra pathing and rendering hooks where supported by the branch and runtime.
- Click-to-path selection and area commands for fast interactive control.

## Visual Language
- Default overlay palette now uses Hypervision crimson and slate tones.
- Chat prefixes, toast titles, selection outlines, and path overlays follow the new brand colors.
- Minecraft chat formatting falls back to `RED`, `DARK_GRAY`, and `GRAY` where full RGB styling is unavailable.

## Compatibility Notes
- `hypervision.*` is the canonical root for new work.
- Legacy `baritone.*` code remains in-tree during transition to preserve compatibility paths and migration support.
