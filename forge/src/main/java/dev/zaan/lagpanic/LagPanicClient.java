package dev.zaan.lagpanic;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import dev.zaan.lagpanic.ui.SettingsScreen;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;


public final class LagPanicClient {
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
        return active ? "PANIC: " + config.profile : "STANDBY";
    }

    private static final KeyMapping PANIC = new KeyMapping(
            "key.lagpanic.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8,
            "key.categories.lagpanic");
    private static final KeyMapping SETTINGS = new KeyMapping(
            "key.lagpanic.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F9,
            "key.categories.lagpanic");

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(PANIC);
        event.register(SETTINGS);
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        if (config == null) config = Config.load();
        if (!attemptedRecovery) {
            attemptedRecovery = true;
            recoveryAllowed = GraphicsSnapshot.restorePending(client);
        }
        while (PANIC.consumeClick()) toggle(client);
        while (SETTINGS.consumeClick()) openSettings(client);
        tick(client);
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
        boolean lowFps = config.autoWhenSlowFps && FRAMES.sustainedLowFps(config.lowFpsThreshold);
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
