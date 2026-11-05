# Lag Panic ⚡
Minecraft **26.3** client-side Fabric mod by **zaan**.

**Status:** experimental / awaiting gameplay testing. No benchmarked FPS improvement is claimed.

## Why
When Minecraft starts lagging, press **F8** to temporarily lower a few expensive graphics options. Press F8 again to restore the previous values.

Use **Right Shift** to configure independent toggles for render distance, particles, entity distance and clouds. Choose **Light / Medium / Nuclear**.

- Restores original values when panic ends.
- Recovers settings after an unexpected game close via `config/lagpanic-recovery.json`.
- Auto detection optional and off by default: detects repeated client-tick stalls, then activates panic mode. Auto restore after sustained stability.
- Configuration saved to `config/lagpanic.json`.
- Does not change world data, simulation distance, mods or the game's backend.

> Changing render distance can itself cause chunk rebuild stalls. Especially aggressive auto mode may be counterproductive. No claims of guaranteed FPS improvement.

## Install
Create Minecraft 26.3 Fabric instance in Prism Launcher. Install matching Fabric API, then put the mod JAR from a **successful** GitHub Actions build into Mods. Use a spare test world at first.

## Build
Requires JDK 25 and Gradle 9.6; run `gradle clean build`. GitHub Actions builds the versioned `lagpanic-0.1.0.jar` and publishes it as a workflow artifact. GitHub bundles Actions artifacts into a ZIP transport, but the **mod itself is a JAR**. No release or Modrinth upload until runtime testing.

See [implementation limitations](docs/BEHAVIOR.md).

License: MIT.
