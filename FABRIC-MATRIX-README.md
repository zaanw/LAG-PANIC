# Lag Panic — Fabric multi-version development

This branch contains an isolated Fabric build matrix for Minecraft 1.21.1–1.21.11. See [docs/FABRIC-1.21.X.md](docs/FABRIC-1.21.X.md) for the pinned Fabric API dependencies and test gates.

Requirements: JDK 21, Gradle 9.2.1. Example:

```bash
gradle clean build -Pminecraft_version=1.21.8 -Pfabric_api_version=0.136.1+1.21.8
```

The output jar is version-specific. Build success does not establish in-game compatibility; each artifact still needs a production-remapped client smoke test.
