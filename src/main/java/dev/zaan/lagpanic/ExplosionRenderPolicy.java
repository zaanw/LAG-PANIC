package dev.zaan.lagpanic;

/**
 * Client-rendering policy only: does not stop TNT ticks, entity updates,
 * block destruction, or server-side explosion processing.
 */
public final class ExplosionRenderPolicy {
    private ExplosionRenderPolicy() { }

    public static boolean hidePrimedTnt(boolean active, Config config) {
        return active && config != null &&
                (config.hidePrimedTnt || nuclearGuardEnabled(config));
    }

    public static boolean hideExplosionParticles(boolean active, Config config) {
        return active && config != null &&
                (config.hideExplosionParticles || nuclearGuardEnabled(config));
    }

    private static boolean nuclearGuardEnabled(Config config) {
        return config.nuclearExplosionGuard && "Nuclear".equals(config.profile);
    }
}
