# Automatic Modrinth releases (Fabric)

This experimental workflow publishes **new** Modrinth versions from GitHub Release tags. It does not edit or delete old Modrinth releases, and it is disabled until its workflow is merged into the repository's default branch and credentials are configured.

## One-time configuration (repository owner)

1. Go to Modrinth account settings and create a personal access token with **VERSION_CREATE** permission for publishing to your project.
2. On GitHub, open **Settings → Secrets and variables → Actions → Secrets**. Add a *repository secret* named `MODRINTH_TOKEN`. Paste the token there; **never** paste it into issues, commits, or chats.
3. In the adjacent **Variables** tab, add repository variable `MODRINTH_PROJECT_ID` with the project's actual Modrinth ID.
4. Review and merge `.github/workflows/publish-modrinth.yml` along with the tested Fabric 1.21.x source into the repository's default branch. The release tag must point at that source.

## Release procedure

1. Test the new build in Minecraft and inspect successful GitHub Actions build results.
2. On GitHub, create a new **Release** with a **unique new tag** such as `v0.4.0` from the tested branch/commit; add meaningful release notes.
3. Publish the release (not merely save a draft).
4. GitHub Actions then builds all 11 Fabric 1.21.x variants and uploads each compiled runtime JAR to Modrinth with its own Minecraft version metadata and version number. A failed build prevents that *particular variant* from being uploaded.

The workflow does not trigger on ordinary pushes. It skips GitHub prereleases. Never republish the same tag/version number; Modrinth versions should be unique. The workflow is **not transactional**: if some matrix jobs fail, previously uploaded siblings remain on Modrinth and need review.

Minecraft 1.20.x, Forge, NeoForge and Quilt are **not yet included**. Add those only after separate builds and runtime tests pass.

## Troubleshooting

- Missing MODRINTH_TOKEN or MODRINTH_PROJECT_ID: configure GitHub repository Secret/Variable as above.
- Duplicate version number: create a new release tag for the new update.
- Invalid dependency or API permission: inspect the failing job log before trying again.
- No workflow on release: merge the workflow to the default branch and publish a release tag referencing compatible sources.

Do not publish until the workflow itself has been reviewed and tested.
