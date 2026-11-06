# Lag Panic Forge 1.21.1 (development)

Independent ForgeGradle module in its own branch. Java 21, Minecraft 1.21.1, Forge 52.1.20.

Run `cd forge && gradle clean build` with Gradle 8.10.2 and JDK 21.

F8 toggles reversible emergency graphics presets, F9 opens settings, config and crash recovery persist in the Minecraft config folder. Automatic spike detection is retained.

**Limitations:** Experimental rendering mixins (hide primed TNT/explosion visuals) are intentionally not carried over yet. No support is claimed for 1.21.2 or later: every Minecraft minor version requires a separate verified build. A passing Gradle build alone is not sufficient; check in a real client with backup world before publishing.

Original released Fabric 26.3 beta remains unchanged.
