# Lag Panic — Forge 1.21.x port matrix

Work branch: `feat/forge-matrix`. Base: the isolated Forge 1.21.1 development branch. Do not merge into `main` or publish releases from this branch until every artifact is tested in a real client.

## Official Forge targets

The Forge downloads index lists these Minecraft 1.21.x targets and MDK versions (snapshot checked 2026-11-06):

| Minecraft | Forge MDK | CI status | Client smoke test |
|---|---:|---|---|
| 1.21.1 | 52.1.21 | Pending in matrix | Not run |
| 1.21.2 | No separate official Forge entry found | Skipped; verify later | Not applicable |
| 1.21.3 | 53.1.16 | Pending in matrix | Not run |
| 1.21.4 | 54.1.22 | Pending in matrix | Not run |
| 1.21.5 | 55.1.18 | Pending in matrix | Not run |
| 1.21.6 | 56.0.9 | Pending in matrix | Not run |
| 1.21.7 | 57.0.3 | Pending in matrix | Not run |
| 1.21.8 | 58.1.26 | Pending in matrix | Not run |
| 1.21.9 | 59.0.5 | Pending in matrix | Not run |
| 1.21.10 | 60.1.18 | Pending in matrix | Not run |
| 1.21.11 | 61.2.5 | Pending in matrix | Not run |

Official Forge download pages: [1.21.1](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.1.html), [1.21.3](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.3.html), [1.21.4](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.4.html), [1.21.5](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.5.html), [1.21.6](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.6.html), [1.21.7](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.7.html), [1.21.8](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.8.html), [1.21.9](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.9.html), [1.21.10](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.10.html), [1.21.11](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.11.html). The official index navigation does not list a distinct 1.21.2 Forge target; do not invent a Forge coordinate for it.

## Release gates

- [ ] Each matrix entry resolves the exact Forge dependency and compiles.
- [ ] Unit tests pass for every target.
- [ ] A separate client smoke test passes on each target.
- [ ] F8 toggle, F9 settings, config persistence, restore/recovery, and panic disable behavior are checked.
- [ ] Experimental visual mixins are ported and tested separately or remain explicitly unavailable.
- [ ] Only then prepare version-specific Modrinth files.

A green compile is not a runtime test. No release is produced by this workflow.
