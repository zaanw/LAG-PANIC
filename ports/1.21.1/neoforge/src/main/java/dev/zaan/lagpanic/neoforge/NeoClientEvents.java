package dev.zaan.lagpanic.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import dev.zaan.lagpanic.port.PanicRuntime;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class NeoClientEvents {
    private static final KeyMapping PANIC = new KeyMapping(
            "key.lagpanic.toggle", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8, "key.categories.lagpanic");

    private NeoClientEvents() { }

    public static void initialize() {
        PanicRuntime.initialize(FMLPaths.CONFIGDIR.get());
    }

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(PANIC);
    }

    public static void afterClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        PanicRuntime.onClientTick(client);
        while (PANIC.consumeClick()) PanicRuntime.toggle(client);
    }
}
