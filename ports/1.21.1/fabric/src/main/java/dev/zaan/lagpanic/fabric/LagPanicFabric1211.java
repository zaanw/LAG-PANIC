package dev.zaan.lagpanic.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import dev.zaan.lagpanic.port.PanicRuntime;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class LagPanicFabric1211 implements ClientModInitializer {
    @Override public void onInitializeClient() {
        PanicRuntime.initialize(FabricLoader.getInstance().getConfigDir());
        KeyMapping panic = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.lagpanic.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                "key.categories.lagpanic"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PanicRuntime.onClientTick(client);
            while (panic.consumeClick()) PanicRuntime.toggle(client);
        });
    }
}
