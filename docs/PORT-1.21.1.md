# Minecraft 1.21.1 multi-loader port (work in progress)

Branch: `feat/port-1.21.1-multiloader`. Target: Minecraft **1.21.1**, Java **21**.

## Scope and verification

| Loader | Source adapter | CI compilation | Minecraft runtime |
| --- | --- | --- | --- |
| Fabric | Initial API migration in progress | Not yet verified | Not tested |
| Quilt | Fabric-loader compatibility to be evaluated; no native metadata yet | Not tested | Not tested |
| Forge | Dedicated entrypoint/build/mixins required | Not implemented | Not tested |
| NeoForge | Dedicated entrypoint/build/mixins required | Not implemented | Not tested |

Do not publish or claim a supported version based on this branch until its corresponding build and client smoke tests pass.

## Port checklist

- [x] Isolate changes in a feature branch; leave `main` and existing beta tag untouched.
- [x] Switch Fabric build configuration to Minecraft 1.21.1 and Java 21.
- [x] Begin porting keybindings, HUD, settings screen, and rendering mixin method signature.
- [ ] Confirm Fabric Gradle build and JUnit tests with GitHub Actions.
- [ ] Run a Minecraft 1.21.1 Fabric client to verify both keybinds, UI, panic activation/restore, and crash recovery.
- [ ] Smoke test the Fabric artifact under Quilt and confirm metadata/dependency compatibility.
- [ ] Extract platform-neutral diagnostics, config, presets and recovery logic into shared source.
- [ ] Implement Forge bootstrap, event and key mappings, render hooks and build.
- [ ] Implement NeoForge bootstrap, event and key mappings, render hooks and build.
- [ ] Build/test each loader independently, then run Minecraft client smoke tests.
- [ ] Open a draft PR for review; do not publish a release from this branch.

### Safety checks

Verify recovery JSON survives forced shutdown, manual changes made while panic mode is active are not overwritten, and visual suppression does not imply TNT physics/explosion logic is disabled. Do not skip mixin and modpack compatibility tests.

### CI

`.github/workflows/build.yml` runs Gradle on feature branches. The existing publication workflow is intentionally restricted to `main`; this port must never push to `main`, update a release tag or upload a release asset without explicit approval.
