package dev.zaan.lagpanic.ui;

import dev.zaan.lagpanic.Config;
import dev.zaan.lagpanic.LagPanicClient;
import dev.zaan.lagpanic.MemoryPressure;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SettingsScreen extends Screen {
    private final Screen parent;
    private boolean performancePage;
    private final Config c = LagPanicClient.config;

    public SettingsScreen(Screen parent) {
        super(Component.literal("Lag Panic"));
        this.parent = parent;
    }

    private Button option(int x, int y, int w, String label, Runnable change) {
        return addRenderableWidget(Button.builder(Component.literal(label), b -> {
            change.run();
            c.save();
            rebuildWidgets();
        }).bounds(x, y, w, 20).build());
    }
    private String bool(boolean value) { return value ? "ON" : "OFF"; }

    @Override protected void init() {
        int w = Math.min(170, (width - 32) / 2);
        int left = width / 2 - w - 4;
        int right = width / 2 + 4;
        int y = Math.max(22, height / 2 - 133);
        int row = 24;

        if (performancePage) {
            option(left, y + row, w, "Auto Panic: " + bool(c.auto),
                    () -> c.auto = !c.auto);
            option(right, y + row, w, "Low-FPS trigger: " + bool(c.autoWhenSlowFps),
                    () -> c.autoWhenSlowFps = !c.autoWhenSlowFps);
            option(left, y + row * 2, w, "FPS threshold: " + c.lowFpsThreshold,
                    () -> c.lowFpsThreshold = c.lowFpsThreshold >= 60 ? 15 : c.lowFpsThreshold + 5);
            option(right, y + row * 2, w, "Cooldown: " + c.autoCooldownSeconds + "s",
                    () -> c.autoCooldownSeconds = c.autoCooldownSeconds >= 600 ? 30 : c.autoCooldownSeconds + 30);
            option(left, y + row * 3, w, "Memory HUD: " + bool(c.debugOverlay),
                    () -> c.debugOverlay = !c.debugOverlay);
            option(right, y + row * 3, w, "GC experiment: " + bool(c.allowExperimentalGc),
                    () -> c.allowExperimentalGc = !c.allowExperimentalGc);
            option(left, y + row * 4, w, "GC threshold: " + c.gcThresholdPercent + "%",
                    () -> c.gcThresholdPercent = c.gcThresholdPercent >= 95 ? 70 : c.gcThresholdPercent + 5);
            option(right, y + row * 4, w, "Stable: " + c.stableSecondsToRestore + "s",
                    () -> c.stableSecondsToRestore = c.stableSecondsToRestore >= 300 ? 20 : c.stableSecondsToRestore + 10);
            option(left, y + row * 5, w, "Simulation dist: " + bool(c.changeSimulationDistance),
                    () -> c.changeSimulationDistance = !c.changeSimulationDistance);
            option(right, y + row * 5, w, "Hide Primed TNT: " + bool(c.hidePrimedTnt),
                    () -> c.hidePrimedTnt = !c.hidePrimedTnt);
            option(left, y + row * 6, w, "Hide Explosions: " + bool(c.hideExplosionParticles),
                    () -> c.hideExplosionParticles = !c.hideExplosionParticles);
            addRenderableWidget(Button.builder(Component.literal("< Back"), b -> {
                performancePage = false;
                rebuildWidgets();
            }).bounds(width / 2 - 70, y + row * 7, 140, 20).build());
            return;
        }

        option(left, y, w, "Panic: " + (LagPanicClient.isActive() ? "ON" : "OFF"),
                () -> LagPanicClient.toggle(minecraft));
        option(right, y, w, "Auto: " + bool(c.auto), () -> c.auto = !c.auto);

        Button profile = option(left, y + row, w, "Profile: " + c.profile, () -> c.profile = switch (c.profile) {
            case "Light" -> "Medium";
            case "Medium" -> "Nuclear";
            default -> "Light";
        });
        profile.active = !LagPanicClient.isActive();

        option(right, y + row, w, "Auto restore: " + bool(c.restoreAutomatically),
                () -> c.restoreAutomatically = !c.restoreAutomatically);

        Button render = option(left, y + row * 2, w, "Render distance: " + bool(c.changeRenderDistance),
                () -> c.changeRenderDistance = !c.changeRenderDistance);
        Button entities = option(right, y + row * 2, w, "Entity distance: " + bool(c.changeEntityDistance),
                () -> c.changeEntityDistance = !c.changeEntityDistance);
        Button particles = option(left, y + row * 3, w, "Particles: " + bool(c.changeParticles),
                () -> c.changeParticles = !c.changeParticles);
        Button clouds = option(right, y + row * 3, w, "Clouds: " + bool(c.changeClouds),
                () -> c.changeClouds = !c.changeClouds);

        render.active = entities.active = particles.active = clouds.active = !LagPanicClient.isActive();

        option(left, y + row * 4, w, "Spike: " + c.spikeThresholdMs + " ms",
                () -> c.spikeThresholdMs = c.spikeThresholdMs >= 2000 ? 125 : c.spikeThresholdMs + 125);
        option(right, y + row * 4, w, "Spikes needed: " + c.spikesToTrigger,
                () -> c.spikesToTrigger = c.spikesToTrigger >= 10 ? 2 : c.spikesToTrigger + 1);

        option(left, y + row * 5, w, "Stable: " + c.stableSecondsToRestore + " sec",
                () -> c.stableSecondsToRestore = c.stableSecondsToRestore >= 300 ? 20 : c.stableSecondsToRestore + 10);
        option(right, y + row * 5, w, "Cooldown: " + c.autoCooldownSeconds + " sec",
                () -> c.autoCooldownSeconds = c.autoCooldownSeconds >= 600 ? 30 : c.autoCooldownSeconds + 30);

        option(left, y + row * 6, w, "Stage interval: " + c.stageIntervalTicks + " ticks",
                () -> c.stageIntervalTicks = c.stageIntervalTicks >= 40 ? 1 : c.stageIntervalTicks + 2);
        option(right, y + row * 6, w, "Debug HUD: " + bool(c.debugOverlay),
                () -> c.debugOverlay = !c.debugOverlay);

        option(left, y + row * 7, w, "Experimental GC: " + bool(c.allowExperimentalGc),
                () -> c.allowExperimentalGc = !c.allowExperimentalGc);
        option(right, y + row * 7, w, "GC threshold: " + c.gcThresholdPercent + "%",
                () -> c.gcThresholdPercent = c.gcThresholdPercent >= 95 ? 70 : c.gcThresholdPercent + 5);

        addRenderableWidget(Button.builder(Component.literal("Performance >"), b -> {
                performancePage = true;
                rebuildWidgets();
            }).bounds(left, y + row * 8 + 2, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(right, y + row * 8 + 2, w, 20).build());
    }

    @Override public void render(GuiGraphics graphics, int mx, int my, float partialTick) {
        super.render(graphics, mx, my, partialTick);
        graphics.drawCenteredString(font, Component.literal(performancePage ? "LAG PANIC  /  PERFORMANCE" : "LAG PANIC  /  MIDNIGHT"),
                width / 2, Math.max(7, height / 2 - 157), 0xFF77D3EF);
        MemoryPressure.Heap heap = LagPanicClient.heap();
        graphics.drawCenteredString(font, Component.literal(
                "JVM heap: " + heap.usedMiB() + " / " + heap.maxMiB() + " MiB (" + heap.percent() + "%)"),
                width / 2, Math.min(height - 30, Math.max(18, height / 2 + 101)), 0xFF77D3EF);
        graphics.drawCenteredString(font, Component.literal(performancePage
                    ? "Stalls: " + LagPanicClient.diagnostics().count()
                        + " | Last: " + LagPanicClient.diagnostics().lastPauseMillis()
                        + "ms | GC observed: " + LagPanicClient.diagnostics().lastGcDeltaMillis() + "ms"
                    : "F8 Panic | F9 Settings | Performance >"),
                width / 2, Math.min(height - 12, Math.max(28, height / 2 + 114)), 0xFF8795A8);
        if (performancePage) {
            var telemetry = LagPanicClient.telemetry();
            graphics.drawCenteredString(font, Component.literal(
                    "Session: " + telemetry.manualActivations() + " manual / "
                    + telemetry.autoActivations() + " auto | Panic time: "
                    + telemetry.activeSeconds(System.nanoTime()) + "s | Last: "
                    + telemetry.lastTriggerLabel()),
                    width / 2, Math.min(height - 12, Math.max(28, height / 2 + 127)), 0xFF77D3EF);
        }
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
}
