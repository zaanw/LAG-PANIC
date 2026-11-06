package dev.zaan.lagpanic;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

/** NeoForge loader entrypoint. Gameplay handlers are installed only on physical clients. */
@Mod("lagpanic")
public final class LagPanicNeoForge {
    public LagPanicNeoForge(IEventBus modBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            LagPanicClient.initialize(modBus);
        }
    }
}
