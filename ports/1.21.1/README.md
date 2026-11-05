# Lag Panic — Minecraft 1.21.1 (work in progress)

**NOT RELEASE READY.** This is a separate experiment for Fabric and NeoForge. The existing Minecraft 26.3 release on `main` is not modified.

## Scope and status

- [x] Isolated GitHub branch `port/1.21.1-fabric-neoforge`
- [x] Extracted loader-neutral spike detector, frame monitor, freeze diagnostics and presets
- [ ] Run smoke tests in GitHub Actions (workflow provided; result must be checked)
- [ ] Fabric 1.21.1 loader integration compiled and verified
- [ ] NeoForge 1.21.1 loader integration compiled and verified
- [ ] Keybinds F8/F9 and settings UI parity
- [ ] Crash-safe backup/restore across both loaders
- [ ] TNT and explosion particle mixin mappings validated in game
- [ ] Test with Sodium and common modpacks
- [ ] Publish only after both loaders build and in-game tests pass

## Java compatibility

Minecraft 1.21.1 targets Java 21, unlike the current Minecraft 26.3 release (Java 25).

## Architecture

The `common` subtree holds code that uses no Fabric/NeoForge classes.
Loader-specific modules must provide event registration, keybinds, configuration paths, client HUD hooks, rendering mixins and Gradle metadata.
Do not claim these targets are supported in Modrinth metadata until real runnable JARs are produced and tested.

## Testing the shared core

Use the GitHub Actions `1.21.1 port shared core` workflow or locally run:

```sh
mkdir -p /tmp/lagpanic-classes
javac -d /tmp/lagpanic-classes common/src/main/java/dev/zaan/lagpanic/*.java common/src/test/java/dev/zaan/lagpanic/CommonSmokeTest.java
java -cp /tmp/lagpanic-classes dev.zaan.lagpanic.CommonSmokeTest
```

Run these commands from `ports/1.21.1`.
