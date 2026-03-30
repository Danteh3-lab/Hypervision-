# Hypervision Setup

## Install
The easiest way to use Hypervision is as a Fabric mod build. This branch is intentionally trimmed to the Fabric/common code path only.

Until official Hypervision release pages exist, use local builds from this repository.

## Build Requirements
The practical recommendation for this branch is Java 17 when running Gradle verification tasks.

Why: this branch currently hits a Gradle 8.2 `buildSrc.jar` instrumentation issue under Java 21 before project tests run cleanly.

## Build From Source
### Command Line
- Windows: `gradlew build`
- macOS/Linux: `./gradlew build`

The default project setup in this branch builds the shared common code and the Fabric module.

### IntelliJ
- Open the repository as a Gradle project.
- Refresh the Gradle model after import.
- Use the Java version expected by the active Minecraft branch, with Java 17 preferred for this branch's current build flow.

## Build Outputs
Build artifacts are written into `dist`.

Expect Hypervision-branded artifacts, including variants whose names start with `hypervision-`.

Typical outputs include:
- API-focused jars for downstream integrations
- Standalone/obfuscated jars for direct use
- Unoptimized jars that are easier to inspect in crash reports

## Runtime Storage
Hypervision now prefers:
- `minecraft/hypervision/settings.txt`
- `minecraft/hypervision` cache/output directories

Legacy `minecraft/baritone` data is still read when present so existing local settings and cache data are not stranded.

## Placeholder Links
- Releases: <https://example.com/hypervision/releases>
- Repository: <https://example.com/hypervision/repository>
- Issues: <https://example.com/hypervision/issues>
