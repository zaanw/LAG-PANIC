package dev.zaan.lagpanic;

/** Target limits, never raises a player's quality settings while panicking. */
public record Preset(int renderDistance, double entityDistance) {
    public static Preset forName(String name) {
        return switch (name) {
            case "Light" -> new Preset(12, 0.8);
            case "Nuclear" -> new Preset(4, 0.5);
            default -> new Preset(8, 0.65);
        };
    }
}
