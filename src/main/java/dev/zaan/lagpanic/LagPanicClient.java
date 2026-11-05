package dev.zaan.lagpanic;

import com.mojang.blaze3d.platform.InputConstants;
import dev.zaan.lagpanic.ui.SettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class LagPanicClient implements ClientModInitializer {
    public static Config config;
    private static final SpikeDetector DETECTOR = new SpikeDetector();
    private static GraphicsSnapshot snapshot;
    private static boolean active;
    private static boolean attemptedRecovery;
    private static long activatedAt;
    private static long lastAutomaticChange;
    private static boolean autoActivated;

    public static boolean isActive() { return active; }
    @Override public void onInitializeClient() {
        config = Config.load();
        KeyMapping.Category category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("lagpanic", "general"));
        KeyMapping panic = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.lagpanic.toggle", InputConstants.Type.KEYBOARD, InputConstants.KEY_F8, category));
        KeyMapping settings = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.lagpanic.settings", InputConstants.Type.KEYBOARD, InputConstants.KEY_RSHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!attemptedRecovery) {
                attemptedRecovery = true;
                GraphicsSnapshot.restorePending(client);
            }
            while (panic.consumeClick()) toggle(client);
            while (settings.consumeClick()) client.gui.setScreen(new SettingsScreen(client.gui.screen()));
            tick(client);
        });
    }

    public static void toggle(Minecraft client) {
        if (active) deactivate(client);
        else activate(client, false);
    }

    public static void activate(Minecraft client, boolean automatically) {
        if (active || client.level == null) return;
        GraphicsSnapshot s = GraphicsSnapshot.capture(client.options);
        // Persist recovery before changing any settings.
        s.persist();
        s.apply(client.options, config);
        snapshot = s;
        snapshot.persist(); // Store the specific applied values used for safe restoration.
        active = true;
        autoActivated = automatically;
        activatedAt = System.nanoTime();
        lastAutomaticChange = activatedAt;
    }

    public static void deactivate(Minecraft client) {
        if (!active) return;
        if (snapshot != null) {
            snapshot.recover(client.options);
            client.options.save();
        }
        GraphicsSnapshot.clearBackup();
        snapshot = null;
        active = false;
        autoActivated = false;
        DETECTOR.reset();
        lastAutomaticChange = System.nanoTime();
    }

    private static void tick(Minecraft client) {
        if (client.level == null || client.player == null) {
            if (active) deactivate(client);
            DETECTOR.reset();
            return;
        }
        long now = System.nanoTime();
        if (client.gui.screen() != null) {
            DETECTOR.reset();
            return;
        }
        boolean trigger = DETECTOR.recordTick(now, config.spikeThresholdMs, config.spikesToTrigger);
        if (!config.auto) return;
        if (!active && trigger && now - lastAutomaticChange > 20_000_000_000L) {
            activate(client, true);
        } else if (active && autoActivated && config.restoreAutomatically
                && now - activatedAt > 30_000_000_000L
                && DETECTOR.quietFor(now, config.stableSecondsToRestore)) {
            deactivate(client);
        }
    }
}
