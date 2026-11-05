package dev.zaan.lagpanic.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod("lagpanic")
public final class LagPanicNeo1211 {
    public LagPanicNeo1211(IEventBus modBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(NeoClientEvents::registerKeys);
            NeoForge.EVENT_BUS.addListener(NeoClientEvents::afterClientTick);
            NeoClientEvents.initialize();
        }
    }
}
