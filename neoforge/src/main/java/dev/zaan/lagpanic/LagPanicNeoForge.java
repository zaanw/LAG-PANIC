package dev.zaan.lagpanic;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

/** NeoForge loader entrypoint. Gameplay handlers are installed only on physical clients. */
@Mod("lagpanic")
public final class LagPanicNeoForge {
    public LagPanicNeoForge(IEventBus modBus) {
        if (isPhysicalClient()) {
            LagPanicClient.initialize(modBus);
        }
    }

    private static boolean isPhysicalClient() {
        try {
            Object distribution;
            try {
                distribution = FMLEnvironment.class.getMethod("getDist").invoke(null);
            } catch (NoSuchMethodException legacy) {
                distribution = FMLEnvironment.class.getField("dist").get(null);
            }
            return distribution == Dist.CLIENT;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot determine NeoForge distribution", e);
        }
    }
}
