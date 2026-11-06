# Lag Panic — Fabric 1.21.x matrix

Development branch: `feat/fabric-matrix`, based on the isolated Fabric 1.21.1 port. The released beta and the Forge branches are not modified.

Java 21, Fabric Loader 0.18.4, Fabric Loom 1.14.10 (remapping plugin), Gradle 9.2.1. Loom 1.14+ uses the `net.fabricmc.fabric-loom-remap` plugin ID for obfuscated Minecraft versions such as 1.21.11.

| Minecraft | Fabric API | CI build | Client test |
|---|---|---|---|
| 1.21.1 | 0.116.13+1.21.1 | Pending | Not run |
| 1.21.2 | 0.106.1+1.21.2 | Pending | Not run |
| 1.21.3 | 0.114.1+1.21.3 | Pending | Not run |
| 1.21.4 | 0.119.3+1.21.4 | Pending | Not run |
| 1.21.5 | 0.124.2+1.21.5 | Pending | Not run |
| 1.21.6 | 0.128.2+1.21.6 | Pending | Not run |
| 1.21.7 | 0.128.1+1.21.7 | Pending | Not run |
| 1.21.8 | 0.136.1+1.21.8 | Pending | Not run |
| 1.21.9 | 0.134.0+1.21.9 | Pending | Not run |
| 1.21.10 | 0.138.4+1.21.10 | Pending | Not run |
| 1.21.11 | 0.141.6+1.21.11 | Pending | Not run |

Fabric API coordinates are pinned per Minecraft version; do not reuse a jar/API build across versions. References: official [Fabric Loom docs](https://docs.fabricmc.net/develop/loom/), [Fabric CLI version resolver](https://www.fabricmc.net/develop/cli/), and the linked Fabric API version pages in the build notes.

## Release gates

- Every matrix job must resolve dependencies, compile and pass unit tests.
- Run each artifact in its exact Minecraft/Fabric profile.
- Verify F8 activation/deactivation, F9 settings, persistence, backup recovery and crash-free world entry/exit.
- Verify mixins in a production-remapped client, not only the development run.
- Do not publish a Modrinth file until that exact version has passed its client test.

A green CI compile is not a Minecraft runtime test. This branch is development-only and creates no release.
