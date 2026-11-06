# Lag Panic v0.5.0 — NeoForge 1.21.1

Native NeoForge port in `neoforge/`; this is **not** a renamed Fabric or Forge jar.

- Minecraft **1.21.1**
- NeoForge **21.1.265**
- Java **21**
- Dedicated Gradle project using ModDevGradle **2.0.148**

## Features carried from v0.5.0

- Manual Panic toggle **F8**, Midnight settings **F9**.
- Graphics staging, saved crash recovery, and restoration of original game settings.
- Automatic panic triggers for low FPS and slow client ticks.
- Local diagnostics: session activations, freeze/stall data, trigger reason, active duration.
- No remote metrics or user tracking.

## Build and test

```bash
cd neoforge
gradle clean build
```

The dedicated CI workflow `.github/workflows/neoforge-v050.yml` uses Java 21 and runs JUnit tests. If successful it uploads a development JAR and checks the embedded NeoForge loader metadata.

## Release gates

1. **CI build and JUnit tests must pass**.
2. **Start the game on NeoForge 21.1.265 / Minecraft 1.21.1** with the generated jar.
3. Check **F8, F9, Auto Panic, HUD**, graphics restoration, recovery after a forced restart and idle/off mode.
4. Compare performance using repeatable test conditions and actual frame timings.
5. Release a **new NeoForge v0.5.0 artifact**, without overwriting Fabric or Forge releases.

Fabric-specific render mixins for hiding primed TNT and explosion particles have **not** been ported to NeoForge. These settings should not be advertised as working until dedicated loader support is implemented and tested.

The existing Fabric publishing pipeline and previously released Modrinth files are untouched.
