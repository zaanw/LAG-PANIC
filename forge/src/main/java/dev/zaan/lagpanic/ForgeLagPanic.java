package dev.zaan.lagpanic;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

/** Forge-specific bootstrap, completely separate from the Fabric entrypoint. */
@Mod(ForgeLagPanic.MOD_ID)
public final class ForgeLagPanic {
    public static final String MOD_ID = "lagpanic";
    public ForgeLagPanic() {
        // Never initialize Minecraft client classes on a dedicated server.
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Client::initialize);
    }

    public static final class Client {
        private static final SpikeDetector SPIKES = new SpikeDetector();
        private static final FrameMonitor FRAMES = new FrameMonitor();
        private static final FreezeDiagnostics FREEZES = new FreezeDiagnostics();
        private static final PanicTelemetry TELEMETRY = new PanicTelemetry();
        private static final MemoryPressure MEMORY = new MemoryPressure();

        private static Config config;
        private static GraphicsSnapshot snapshot;
        private static KeyMapping panicKey;
        private static KeyMapping settingsKey;
        private static boolean active;
        private static boolean attemptedRecovery;
        private static boolean recoveryAllowed;
        private static boolean autoActivated;
        private static long activatedAtNanos;
        private static long lastAutomaticChange;
        private static long lastLowFpsNanos;
        private static int stage;
        private static int stageWait;
        private static int sampleTicks;
        private static int fpsCheckTicks;
        private static MemoryPressure.Heap lastHeap;

        private Client() {}

        public static void initialize() {
            config = Config.load();
            IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
            bus.addListener(Client::registerKeys);
            bus.addListener(Client::registerOverlay);
            MinecraftForge.EVENT_BUS.register(new Client());
        }

        private static void registerKeys(RegisterKeyMappingsEvent event) {
            panicKey = new KeyMapping("key.lagpanic.toggle", InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_F8, "key.categories.lagpanic");
            settingsKey = new KeyMapping("key.lagpanic.settings", InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_F9, "key.categories.lagpanic");
            event.register(panicKey);
            event.register(settingsKey);
        }

        private static void registerOverlay(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("lagpanic_debug", (gui, graphics, partialTick, width, height) -> {
                if (config == null || (!config.debugOverlay && !(config.auto && config.autoWhenSlowFps))) return;
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null || mc.player == null || mc.screen != null) return;
                FRAMES.sample(System.nanoTime());
                if (!config.debugOverlay) return;
                renderDiagnostics(graphics, mc);
            });
        }

        private static void renderDiagnostics(GuiGraphics graphics, Minecraft mc) {
            graphics.fill(5, 5, 315, 68, 0xC81A1C23);
            graphics.drawString(mc.font, "LAG PANIC | " + status(), 11, 11, 0xFFE4EDF7);
            graphics.drawString(mc.font, "FPS~" + FRAMES.estimatedFps()
                    + " | Tick gap " + SPIKES.lastGapMs() + "ms", 11, 23, 0xFF93B0CC);
            graphics.drawString(mc.font, "Long stalls: " + FREEZES.count()
                    + " | Worst: " + FREEZES.worstPauseMillis()
                    + "ms | Avg: " + FREEZES.averagePauseMillis() + "ms", 11, 35, 0xFF93B0CC);
            graphics.drawString(mc.font, "Panic count: " + (TELEMETRY.manualActivations() + TELEMETRY.autoActivations())
                    + " | Time: " + TELEMETRY.activeSeconds(System.nanoTime())
                    + "s | Last: " + TELEMETRY.lastTriggerLabel(), 11, 47, 0xFF93B0CC);
        }

        @SubscribeEvent
        public void onTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (!attemptedRecovery) {
                attemptedRecovery = true;
                recoveryAllowed = GraphicsSnapshot.restorePending(mc);
            }
            if (panicKey != null) while (panicKey.consumeClick()) toggle();
            if (settingsKey != null) while (settingsKey.consumeClick()) {
                if (!(mc.screen instanceof ForgeSettingsScreen))
                    mc.setScreen(new ForgeSettingsScreen(mc.screen));
            }
            tick(mc);
        }

        public static Config config() { return config; }
        public static boolean active() { return active; }
        public static FreezeDiagnostics diagnostics() { return FREEZES; }
        public static PanicTelemetry telemetry() { return TELEMETRY; }
        public static int fps() { return FRAMES.estimatedFps(); }

        public static String status() {
            if (attemptedRecovery && !recoveryAllowed) return "RECOVERY ERROR: check logs";
            return active ? "PANIC: " + config.profile : "OFF";
        }

        public static void toggle() {
            if (active) deactivate();
            else activate(false, PanicTelemetry.Trigger.MANUAL);
        }

        private static void activate(boolean automatically, PanicTelemetry.Trigger trigger) {
            Minecraft mc = Minecraft.getInstance();
            if (active || !recoveryAllowed || mc.level == null || mc.player == null) return;
            if (GraphicsSnapshot.hasPendingBackup() && !GraphicsSnapshot.restorePending(mc)) {
                recoveryAllowed = false;
                return;
            }
            GraphicsSnapshot candidate = GraphicsSnapshot.prepare(mc.options, config);
            if (!candidate.persist()) return;
            snapshot = candidate;
            active = true;
            autoActivated = automatically;
            stage = 0;
            stageWait = 0;
            activatedAtNanos = System.nanoTime();
            lastAutomaticChange = activatedAtNanos;
            TELEMETRY.activated(trigger, activatedAtNanos);
            MEMORY.tryRequest(config, automatically, activatedAtNanos);
        }

        private static void deactivate() {
            if (!active) return;
            Minecraft mc = Minecraft.getInstance();
            if (snapshot != null) {
                snapshot.recover(mc.options);
                mc.options.save();
            }
            // Keep backup if deletion fails so startup recovery is still possible.
            GraphicsSnapshot.clearBackup();
            TELEMETRY.deactivated(System.nanoTime());
            snapshot = null;
            active = false;
            autoActivated = false;
            stage = 0;
            stageWait = 0;
            SPIKES.reset();
            FRAMES.reset();
            lastAutomaticChange = System.nanoTime();
        }

        private static void tick(Minecraft mc) {
            if (config == null || (!active && !config.auto && !config.debugOverlay)) return;
            if (mc.level == null || mc.player == null) {
                if (active) deactivate();
                SPIKES.reset();
                FRAMES.reset();
                lastLowFpsNanos = 0;
                return;
            }
            if (active && snapshot != null && stage < 5 && --stageWait <= 0) {
                snapshot.applyStep(mc.options, stage++);
                stageWait = config.stageIntervalTicks;
            }
            if (mc.screen != null) {
                SPIKES.reset();
                FRAMES.reset();
                return;
            }

            long now = System.nanoTime();
            boolean spikeTrigger = SPIKES.recordTick(now, config.spikeThresholdMs, config.spikesToTrigger);
            boolean longPause = SPIKES.lastGapMs() >= 750;
            if (++sampleTicks >= 20 || longPause) {
                sampleTicks = 0;
                lastHeap = MemoryPressure.sample();
                FREEZES.sample(SPIKES.lastGapMs(), lastHeap);
            }
            if (!config.auto) {
                FRAMES.reset();
                return;
            }
            boolean lowFps = config.autoWhenSlowFps && ++fpsCheckTicks >= 10
                    && FRAMES.sustainedLowFps(config.lowFpsThreshold);
            if (fpsCheckTicks >= 10) fpsCheckTicks = 0;
            if (lowFps) lastLowFpsNanos = now;
            long cooldown = config.autoCooldownSeconds * 1_000_000_000L;
            if (!active && (spikeTrigger || lowFps) && now - lastAutomaticChange >= cooldown) {
                activate(true, lowFps ? PanicTelemetry.Trigger.LOW_FPS : PanicTelemetry.Trigger.TICK_SPIKES);
            } else if (active && autoActivated && config.restoreAutomatically
                    && stage >= 5 && now - activatedAtNanos >= 30_000_000_000L
                    && SPIKES.quietFor(now, config.stableSecondsToRestore)
                    && (lastLowFpsNanos == 0 || now - lastLowFpsNanos >=
                        config.stableSecondsToRestore * 1_000_000_000L)) {
                deactivate();
            }
        }
    }
}
