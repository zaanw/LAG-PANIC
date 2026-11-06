# Lag Panic v0.5.0 — Forge 1.20.1 (development)

This is a **native Forge** port (not a renamed Fabric JAR), located under `forge/`.

- Minecraft: **1.20.1 only**
- Loader: **Forge 47.4.10**
- Java: **17**
- Gradle/ForgeGradle: Gradle **8.8**, ForgeGradle **6.x**
- Source base: Lag Panic **v0.5.0**, with local Panic session telemetry and freeze diagnostics.

## What it does

- F8: toggle Panic mode, with graphics snapshot saved to `config/lagpanic-recovery.json`.
- F9: open a native Forge settings screen (profile, Auto Panic, thresholds and graphics toggles).
- Auto Panic can respond to sustained slow frames or repeated slow client ticks.
- A debug overlay shows FPS, stalls, panic activations, timer and trigger reason.
- Crash recovery attempts to restore original graphics options from the saved snapshot.
- No network telemetry, no automatic upload of crash/diagnostic information.

## Build

From the `forge` directory, with Java 17 and Gradle 8.8:

```bash
gradle clean build
```

The CI workflow `.github/workflows/forge-v050.yml` separately verifies the Forge runtime JAR contains `META-INF/mods.toml`. Forge is not part of the existing 21-Fabric-version publishing workflow.

## Release gates

1. Forge CI Gradle build succeeds.
2. Verify the produced JAR starts on Forge 47.4.10 / Minecraft 1.20.1.
3. Verify F8/F9 keys, manual and automatic Panic, graphics restoration, restart recovery, and client-only use.
4. Verify metadata, naming and actual Forge loader compatibility on Modrinth.
5. Only then publish as a **separate Forge v0.5.0 version**. Do not overwrite Fabric v0.5.0 or v0.4.1.

Caveat: this initial Forge implementation does not yet include the Fabric-specific TNT/explosion particle mixins; feature parity must be tested and tracked explicitly.
