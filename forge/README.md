# Lag Panic Forge 1.21.x (development)

This isolated ForgeGradle module is built per Minecraft minor version. Java 21 is used for the 1.21.x targets.

## Build one target

From this directory, run Gradle with the exact official Forge version, for example:

```bash
gradle clean build -Pminecraft_version=1.21.3 -Pforge_version=53.1.16
```

The output archive name includes the Minecraft version. F8 toggles reversible emergency graphics presets and F9 opens settings. Config and recovery data use the Forge config directory.

**Important:** each target must pass its own Gradle build and a client smoke test. The same source may need version-specific fixes; do not assume a JAR is interchangeable between Minecraft versions. Experimental rendering mixins are not carried into this Forge port yet.

Minecraft 1.21.2 is not in the official Forge downloads index at the time this matrix was prepared, so no Forge artifact/version is guessed for it. The matrix starts at 1.21.3 and includes versions for which the official Forge download pages provide MDK builds.

The original released Fabric beta and the previous Forge 1.21.1 branch remain unchanged.
