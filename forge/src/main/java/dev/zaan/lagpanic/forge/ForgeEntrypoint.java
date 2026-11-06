package dev.zaan.lagpanic.forge;

import dev.zaan.lagpanic.LagPanicClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("lagpanic")
public final class ForgeEntrypoint {
    @Mod.EventBusSubscriber(modid = "lagpanic", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientRegistration {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            LagPanicClient.registerKeys(event);
        }
    }

    @Mod.EventBusSubscriber(modid = "lagpanic", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ClientEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            LagPanicClient.onClientTick(event);
        }

        @SubscribeEvent
        public static void onRenderGui(RenderGuiEvent.Post event) {
            LagPanicClient.onRenderGui(event);
        }
    }
}
