# Lag Panic ⚡

Current development version: **0.2.0-beta.3**. This is not a tested stable release.

Experimental Minecraft **26.3** client-side Fabric mod, maintained by **zaan**.

**Status: pending Minecraft runtime testing.** A green GitHub Actions build only verifies compilation/unit tests. No FPS increase or improved frame-time has been measured.

## What it does

Press **F8** to activate a temporary emergency graphics profile. Press **F8** again to restore previous settings. **Backslash (`\\`)** or **F9** opens settings (both keys can be rebound in Minecraft controls).

Profiles:
- **Light:** cap render distance at 12, entity distance at 0.8.
- **Medium:** cap render distance at 8, entity distance at 0.65.
- **Nuclear:** cap render distance at 4, entity distance at 0.5.
- Particles and clouds are changed only when the profile would **reduce** quality; existing lower settings are preserved.
- Numeric caps never increase a user's graphics settings.

All controls are optional. Changes are spread across multiple client ticks and render distance is changed last. The latest settings are saved in `config/lagpanic.json`.

A durable `config/lagpanic-recovery.json` backup is created **before** touching any graphics settings. Unexpected shutdown recovery runs at next game startup. If recovery fails, Lag Panic disables activation rather than risking another loss of settings.

## Automatic panic

Auto mode is **off by default**. It looks for repeated large **delays between client ticks**; this is NOT GPU frame time or an FPS tracker.

Configurable threshold, number of spikes, recovery stability duration, cooldown and staging interval. It does not constantly switch graphics on isolated spikes. Manual activation is never auto-restored. Auto-triggered panic is restored only after a stable interval.

## JVM memory / experimental GC

Lag Panic now displays **JVM heap used/max** in the settings screen and optional debug HUD.
This does **not** represent total physical RAM, native memory, or video memory.
There is no safe "clear all RAM" operation while Minecraft is running; live chunks, textures, and other game data remain allocated.

**Experimental GC** is OFF by default. When explicitly enabled, a manual F8 activation
can request `System.gc()` only if heap usage exceeds the configurable threshold
(default 85% of max), and at most once per five minutes. Automatic Panic never
requests GC. The JVM can ignore this hint; GC may introduce a significant pause.
It does not promise lower resident RAM or higher FPS. The mod does not regularly
force GC, touch other mods' caches, or override your JVM's Xmx value.

## Debug HUD

Optionally display active mode and latest delay between client ticks. Values are labeled honestly; the HUD does not diagnose the cause of lag.

## Prism Launcher

1. Create a test instance with Minecraft 26.3 and Fabric Loader.
2. Add the matching Fabric API.
3. Download the JAR from a **successful build** (GitHub Actions provides it inside a transport ZIP) and add **only the JAR** in Prism's Mods screen.
4. Test in a backup world. Compare repeatable scenarios with F8 ON/OFF.

The project has a **manual GitHub Release workflow** to publish the JAR directly after a verified in-game test. No release is created automatically.

## Build

Requires JDK 25 + Gradle 9.6: `gradle clean build`. A Gradle wrapper is not yet bundled. GitHub Actions builds the JAR and runs unit tests on commits and PRs.

## Known limitations

- Reducing render distance can itself cause a visible hitch and missing distant chunks during reload.
- Does not diagnose the cause of performance problems, accelerate server TPS, or replace Sodium.
- Different renderers/mods may override the same options; compatibility with Sodium is **not yet runtime-tested**.
- Automated tests do not demonstrate an FPS improvement.
- On short displays, settings may be cramped; UI game testing is pending.

See [technical behavior](docs/BEHAVIOR.md) and [manual test checklist](docs/TESTING.md).

MIT license.
