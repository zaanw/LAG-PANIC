package dev.zaan.lagpanic;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import dev.zaan.lagpanic.ui.SettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import java.lang.reflect.Constructor;


public final class LagPanicClient implements ClientModInitializer {
    public static Config config;
    private static final SpikeDetector DETECTOR = new SpikeDetector();
    private static final FrameMonitor FRAMES = new FrameMonitor();
    private static final FreezeDiagnostics FREEZES = new FreezeDiagnostics();
    private static final MemoryPressure MEMORY = new MemoryPressure();
    private static GraphicsSnapshot snapshot;
    private static boolean active;
    private static boolean recoveryAllowed;
    private static boolean attemptedRecovery;
    private static boolean autoActivated;
    private static long activatedAt;
    private static long lastAutomaticChange;
    private static long lastLowFpsNanos;
    private static int sampleTickCounter;
    private static boolean monitoringEnabled;
    private static int lowFpsCheckTicks;
    private static MemoryPressure.Heap cachedHeap;
    private static int stage = 0;
    private static int stageWait = 0;

    public static boolean isActive() { return active; }
    public static boolean shouldHidePrimedTnt() {
        return active && config != null && config.hidePrimedTnt;
    }
    public static boolean shouldHideExplosionParticles() {
        return active && config != null && config.hideExplosionParticles;
    }
    public static int latestTickPauseMs() { return DETECTOR.lastGapMs(); }
    public static MemoryPressure.Heap heap() {
        if (cachedHeap == null) cachedHeap = MemoryPressure.sample();
        return cachedHeap;
    }
    public static int estimatedFps() { return FRAMES.estimatedFps(); }
    public static FreezeDiagnostics diagnostics() { return FREEZES; }
    public static String status() {
        if (!recoveryAllowed && attemptedRecovery) return "RECOVERY ERROR: check logs";
        return active ? "PANIC: " + config.profile : "OFF";
    }

    @Override public void onInitializeClient() {
        config = Config.load();
        KeyMapping panic = KeyBindingHelper.registerKeyBinding(
                createKeyMapping("key.lagpanic.toggle", GLFW.GLFW_KEY_F8));
        KeyMapping settings = KeyBindingHelper.registerKeyBinding(
                createKeyMapping("key.lagpanic.settings", GLFW.GLFW_KEY_F9));

        HudRenderCallback.EVENT.register(
                (graphics, tracker) -> {

                    // STANDBY without Auto Panic or overlay does no per-frame work.
                    if (!config.debugOverlay && !(config.auto && config.autoWhenSlowFps)) return;
                    Minecraft client = Minecraft.getInstance();
                    if (client.level != null && client.player != null
                            && client.screen == null) {
                        FRAMES.sample(System.nanoTime());
                    }
                    if (!config.debugOverlay) return;
                    if (client.player == null || client.level == null) return;
                    graphics.fill(5, 5, 252, 53, 0xC81A1C23);
                    graphics.drawString(client.font, "LAG PANIC | " + status(), 11, 11, 0xFFE4EDF7);
                    graphics.drawString(client.font, "JVM heap: " + heap().usedMiB() + "/" + heap().maxMiB()
                        + " MiB | FPS~" + estimatedFps() + " | gap " + latestTickPauseMs() + "ms", 11, 23, 0xFF93B0CC);
                    graphics.drawString(client.font, "Long stalls: " + FREEZES.count() + " | Last: "
                        + FREEZES.lastPauseMillis() + "ms | GC observed: "
                        + FREEZES.lastGcDeltaMillis() + "ms", 11, 35, 0xFF93B0CC);
                });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!attemptedRecovery) {
                attemptedRecovery = true;
                recoveryAllowed = GraphicsSnapshot.restorePending(client);
            }
            while (panic.consumeClick()) toggle(client);
            while (settings.consumeClick()) openSettings(client);
            tick(client);
        });
    }

    private static KeyMapping createKeyMapping(String translationKey, int keyCode) {
        try {
            Class<?> categoryClass;
            try {
                categoryClass = Class.forName("net.minecraft.client.KeyMapping$Category");
            } catch (ClassNotFoundException legacyMinecraft) {
                Constructor<KeyMapping> legacyConstructor = KeyMapping.class.getConstructor(
                        String.class, InputConstants.Type.class, int.class, String.class);
                return legacyConstructor.newInstance(
                        translationKey, InputConstants.Type.KEYSYM,
                        keyCode, "key.categories.lagpanic");
            }

            Class<?> identifierClass;
            try {
                identifierClass = Class.forName("net.minecraft.resources.Identifier");
            } catch (ClassNotFoundException legacyName) {
                identifierClass = Class.forName("net.minecraft.resources.ResourceLocation");
            }
            Object identifier = identifierClass.getMethod("parse", String.class)
                    .invoke(null, "lagpanic:main");
            Object category = categoryClass.getMethod("register", identifierClass)
                    .invoke(null, identifier);
            Constructor<?> constructor = KeyMapping.class.getConstructor(
                    String.class, InputConstants.Type.class, int.class, categoryClass);
            return (KeyMapping) constructor.newInstance(
                    translationKey, InputConstants.Type.KEYSYM, keyCode, category);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not create Lag Panic key category", e);
        }
    }

    public static void openSettings(Minecraft client) {
        if (!(client.screen instanceof SettingsScreen))
            client.setScreen(new SettingsScreen(client.screen));
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
        if (!candidate.persist()) return;
        snapshot = candidate;
        active = true;
        autoActivated = automatically;
        stage = 0;
        stageWait = 0;
        activatedAt = System.nanoTime();
        lastAutomaticChange = activatedAt;

        MEMORY.tryRequest(config, automatically, activatedAt);
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
        // Idle STANDBY must not constantly query memory / GC, scan frame windows,
        // or inspect tick spikes. Keep keybindings active for instant activation.
        if (!active && !config.auto && !config.debugOverlay) {
            if (monitoringEnabled) {
                DETECTOR.reset();
                FRAMES.reset();
                cachedHeap = null;
                lastLowFpsNanos = 0L;
                sampleTickCounter = 0;
                lowFpsCheckTicks = 0;
                monitoringEnabled = false;
            }
            return;
        }
        monitoringEnabled = true;
        if (client.level == null || client.player == null) {
            if (active) deactivate(client);
            DETECTOR.reset();
            FRAMES.reset();
            lastLowFpsNanos = 0L;
            return;
        }

        if (active && snapshot != null && stage < 5 && --stageWait <= 0) {
            snapshot.applyStep(client.options, stage++);
            stageWait = config.stageIntervalTicks;
        }

        long now = System.nanoTime();
        if (client.screen != null) {
            DETECTOR.reset();
            FRAMES.reset();
            return;
        }
        boolean spikeTrigger = DETECTOR.recordTick(now, config.spikeThresholdMs, config.spikesToTrigger);

        boolean longPause = DETECTOR.lastGapMs() >= 750;
        if (++sampleTickCounter >= 20 || longPause) {
            sampleTickCounter = 0;
            cachedHeap = MemoryPressure.sample();
            FREEZES.sample(DETECTOR.lastGapMs(), cachedHeap);
        }
        if (!config.auto) {
            FRAMES.reset();
            return;
        }
        // Sampling is per-frame, but checking the 120-frame window every client tick
        // is unnecessary. Ten ticks between decisions keeps this responsive.
        boolean lowFps = config.autoWhenSlowFps && ++lowFpsCheckTicks >= 10
                && FRAMES.sustainedLowFps(config.lowFpsThreshold);
        if (lowFpsCheckTicks >= 10) lowFpsCheckTicks = 0;
        if (lowFps) lastLowFpsNanos = now;
        boolean trigger = spikeTrigger || lowFps;

        long coolDown = config.autoCooldownSeconds * 1_000_000_000L;
        if (!active && trigger && now - lastAutomaticChange >= coolDown) {
            activate(client, true);
        } else if (active && autoActivated && config.restoreAutomatically
                && stage >= 5
                && now - activatedAt >= 30_000_000_000L
                && DETECTOR.quietFor(now, config.stableSecondsToRestore)
                && (lastLowFpsNanos == 0 || now - lastLowFpsNanos
                    >= config.stableSecondsToRestore * 1_000_000_000L)) {
            deactivate(client);
        }
    }
}
