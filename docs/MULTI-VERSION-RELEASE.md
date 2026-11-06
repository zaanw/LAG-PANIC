# 21-version Fabric release pipeline

This workflow has two modes:

- **Verification:** push to `feat/autorlease-21-fabric-versions` or open a PR changing the workflow. It builds all 21 variants, runs unit tests and publishes only private GitHub Actions build artifacts. It **does not upload to Modrinth**.
- **Publishing:** once reviewed and merged into the default branch, a **new stable GitHub Release** triggers builds and uploads one runnable JAR per version to Modrinth. Existing Modrinth versions remain untouched.

## Source pinning

1.20–1.20.6 builds use source commit `713d68f378137ae528c43a58122df606d9c20ea7` from `feat/fabric-1.20.x`.

1.21.1–1.21.11 builds use the GitHub Release **tag** source. This is the current main-line mod.

26.1–26.3 builds use commit `7c17d3092f5c473da11513613809018bd5b26aa4` from `feat/fabric-26x-matrix`.

The 26.x port has a distinct codebase evolved from the 26.3 beta and is **not feature-parity verified** with the 1.21.x mainline. The 1.20.x port has separate compatibility changes. Changing the pins requires a new reviewed workflow commit and fresh tests.

## Release checklist

1. Inspect CI matrix success for all 21 variants. A successful build does **not** prove the client can run the mod.
2. Launch all variant JARs in matching Minecraft/Fabric/API runtime environments. Check F8, F9, OFF status, restoring video settings, and startup/recovery.
3. Verify `MODRINTH_PROJECT_ID` GitHub Actions variable contains the correct Modrinth **project ID**, not project slug. Verify `MODRINTH_TOKEN` GitHub Actions secret has VERSION_CREATE.
4. Merge tested workflow into default branch; only then create a new, **unique** stable GitHub Release tag (e.g. `v0.4.1`) from the tested main commit.
5. Review all 21 GitHub Actions jobs and confirm successful Modrinth uploads.

This workflow is not transactional: if 1 job fails after others publish, do not rerun every successful job and create duplicates. Review and rerun only failures after fixing causes.

**DO NOT create a release based only on green compilation.**
