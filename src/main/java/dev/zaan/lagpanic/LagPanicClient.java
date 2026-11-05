package dev.zaan.lagpanic;

import com.mojang.blaze3d.platform.InputConstants;
import dev.zaan.lagpanic.ui.SettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class LagPanicClient implements ClientModInitializer {
    public static Config config;
    private static final SpikeDetector DETECTOR = new SpikeDetector();
    private static GraphicsSnapshot snapshot;
    private static boolean active;
    private static boolean recoveryAllowed;
    private static boolean attemptedRecovery;
    private static boolean autoActivated;
    private static long activatedAt;
    private static long lastAutomaticChange;
    private static int stage = 0;
    private static int stageWait = 0;

    public static boolean isActive() { return active; }
    public static int latestTickPauseMs() { return DETECTOR.lastGapMs(); }
    public static String status() {
        if (!recoveryAllowed && attemptedRecovery) return "RECOVERY ERROR: check logs";
        return active ? "PANIC: " + config.profile : "STANDBY";
    }

    @Override public void onInitializeClient() {
        config = Config.load();
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("lagpanic", "general"));
        KeyMapping panic = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.lagpanic.toggle", InputConstants.Type.KEYBOARD,
                        InputConstants.KEY_F8, category));
        KeyMapping settings = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.lagpanic.settings", InputConstants.Type.KEYBOARD,
                        InputConstants.KEY_RSHIFT, category));

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("lagpanic", "debug"),
                (graphics, tracker) -> {
                    if (!config.debugOverlay) return;
                    Minecraft client = Minecraft.getInstance();
                    if (client.player == null || client.level == null) return;
                    graphics.fill(5, 5, 226, 39, 0xC81A1C23);
                    graphics.text(client.font, "LAG PANIC | " + status(), 11, 11, 0xFFE4EDF7);
                    graphics.text(client.font, "Tick gap: " + latestTickPauseMs()
                        + "ms (not FPS) | F8: toggle", 11, 23, 0xFF93B0CC);
                });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!attemptedRecovery) {
                attemptedRecovery = true;
                recoveryAllowed = GraphicsSnapshot.restorePending(client);
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
        if (active || !recoveryAllowed || client.level == null || client.player == null) return;
        if (GraphicsSnapshot.hasPendingBackup()) {
            recoveryAllowed = GraphicsSnapshot.restorePending(client);
            if (!recoveryAllowed) return;
        }
        GraphicsSnapshot candidate = GraphicsSnapshot.prepare(client.options, config);
        if (!candidate.persist()) return; // No option changes without durable rollback record.
        snapshot = candidate;
        active = true;
        autoActivated = automatically;
        stage = 0;
        stageWait = 0;
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
        stage = 0;
        stageWait = 0;
        DETECTOR.reset();
        lastAutomaticChange = System.nanoTime();
    }

    private static void tick(Minecraft client) {
        if (client.level == null || client.player == null) {
            if (active) deactivate(client);
            DETECTOR.reset();
            return;
        }
        // One graphics change each N ticks. Changing render distance can cause
        // a chunk rebuild: it is deliberately applied last.
        if (active && snapshot != null && stage < 4 && --stageWait <= 0) {
            snapshot.applyStep(client.options, stage++);
            stageWait = config.stageIntervalTicks;
        }

        long now = System.nanoTime();
        if (client.gui.screen() != null) {
            DETECTOR.reset();
            return;
        }
        boolean trigger = DETECTOR.recordTick(now, config.spikeThresholdMs, config.spikesToTrigger);
        if (!config.auto) return;

        long coolDown = config.autoCooldownSeconds * 1_000_000_000L;
        if (!active && trigger && now - lastAutomaticChange >= coolDown) {
            activate(client, true);
        } else if (active && autoActivated && config.restoreAutomatically
                && stage >= 4
                && now - activatedAt >= 30_000_000_000L
                && DETECTOR.quietFor(now, config.stableSecondsToRestore)) {
            deactivate(client);
        }
    }
}
