# Hypervision Usage

Assuming you already have Hypervision built or installed, the default chat prefix is `#`.

## First Commands
- `#help`
- `#goto 1000 500`
- `#mine diamond_ore`
- `#stop`
- `#surface`
- `#wp save user home`

## Common Commands
- `goto x y z` or `goto x z`: path immediately to a coordinate.
- `goal x y z`, `goal x z`, or `goal y`: set a goal without starting movement until `path`.
- `path`: begin pathing to the current goal.
- `stop`, `cancel`, `forcecancel`: stop active automation.
- `mine <block>`: mine one or more target blocks.
- `follow player <name>`: follow a player.
- `build <file>`: build a schematic from the schematics directory.
- `farm <range>`: harvest and replant in an area.
- `explore [x z]`: expand exploration from a point.
- `wp`: manage waypoints.
- `set`: inspect or edit settings.

## Settings
Examples:
- `#allowBreak`
- `#allowSprint`
- `#primaryTimeoutMS 2500`
- `#modified`
- `#reset`

Hypervision now prefers `minecraft/hypervision/settings.txt`, while still reading legacy `minecraft/baritone/settings.txt` if it exists.

## Prefix and Chat Safety
- `#` is the safest default because it avoids accidentally sending mistyped commands to public chat.
- If direct chat control is enabled and you do not use a prefix, typos can still go to normal chat.

## Visual Rebrand
You should now see Hypervision branding in chat, toast notifications, and default overlay colors.

## Compatibility Note
The canonical API root is now `hypervision.*`, but legacy `baritone.*` code remains in the repository while the transition is in progress.

## Placeholder Links
- Settings reference: <https://example.com/hypervision/settings>
- Issues: <https://example.com/hypervision/issues>
- Community: <https://example.com/hypervision/community>
