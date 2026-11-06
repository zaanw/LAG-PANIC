package dev.zaan.lagpanic;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Native Forge Minecraft 1.20.1 settings screen, no Fabric dependencies. */
public final class ForgeSettingsScreen extends Screen {
    private final Screen parent;
    private boolean performance;
    private final Config c = ForgeLagPanic.Client.config();

    public ForgeSettingsScreen(Screen parent) {
        super(Component.literal("Lag Panic Forge"));
        this.parent = parent;
    }

    private String on(boolean value) { return value ? "ON" : "OFF"; }

    private void add(int x, int y, int width, String label, Runnable command) {
        addRenderableWidget(Button.builder(Component.literal(label), b -> {
            command.run();
            c.save();
            rebuildWidgets();
        }).bounds(x, y, width, 20).build());
    }

    @Override protected void init() {
        int w = Math.max(90, Math.min(175, (width - 24) / 2));
        int left = width / 2 - w - 3;
        int right = width / 2 + 3;
        int top = Math.max(26, height / 2 - 114);
        if (performance) {
            add(left, top, w, "Auto Panic: " + on(c.auto), () -> c.auto = !c.auto);
            add(right, top, w, "Low-FPS trigger: " + on(c.autoWhenSlowFps),
                    () -> c.autoWhenSlowFps = !c.autoWhenSlowFps);
            add(left, top + 25, w, "FPS threshold: " + c.lowFpsThreshold,
                    () -> c.lowFpsThreshold = c.lowFpsThreshold >= 60 ? 15 : c.lowFpsThreshold + 5);
            add(right, top + 25, w, "Restore auto: " + on(c.restoreAutomatically),
                    () -> c.restoreAutomatically = !c.restoreAutomatically);
            add(left, top + 50, w, "Debug HUD: " + on(c.debugOverlay),
                    () -> c.debugOverlay = !c.debugOverlay);
            add(right, top + 50, w, "Stable: " + c.stableSecondsToRestore + "s",
                    () -> c.stableSecondsToRestore = c.stableSecondsToRestore >= 300 ? 20
                            : c.stableSecondsToRestore + 10);
            add(left, top + 75, w, "Cooldown: " + c.autoCooldownSeconds + "s",
                    () -> c.autoCooldownSeconds = c.autoCooldownSeconds >= 600 ? 30
                            : c.autoCooldownSeconds + 30);
            add(right, top + 75, w, "Spike: " + c.spikeThresholdMs + "ms",
                    () -> c.spikeThresholdMs = c.spikeThresholdMs >= 2000 ? 125 : c.spikeThresholdMs + 125);
        } else {
            add(left, top, w, "Panic: " + (ForgeLagPanic.Client.active() ? "ON" : "OFF"),
                    ForgeLagPanic.Client::toggle);
            add(right, top, w, "Auto: " + on(c.auto), () -> c.auto = !c.auto);
            add(left, top + 25, w, "Profile: " + c.profile, () -> {
                if (ForgeLagPanic.Client.active()) return;
                c.profile = switch (c.profile) {
                    case "Light" -> "Medium";
                    case "Medium" -> "Nuclear";
                    default -> "Light";
                };
            });
            add(right, top + 25, w, "Render dist: " + on(c.changeRenderDistance),
                    () -> c.changeRenderDistance = !c.changeRenderDistance);
            add(left, top + 50, w, "Simulation: " + on(c.changeSimulationDistance),
                    () -> c.changeSimulationDistance = !c.changeSimulationDistance);
            add(right, top + 50, w, "Particles: " + on(c.changeParticles),
                    () -> c.changeParticles = !c.changeParticles);
            add(left, top + 75, w, "Entities: " + on(c.changeEntityDistance),
                    () -> c.changeEntityDistance = !c.changeEntityDistance);
            add(right, top + 75, w, "Clouds: " + on(c.changeClouds),
                    () -> c.changeClouds = !c.changeClouds);
        }
        addRenderableWidget(Button.builder(Component.literal(performance ? "< Main" : "Performance >"), b -> {
            performance = !performance;
            rebuildWidgets();
        }).bounds(left, top + 105, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(right, top + 105, w, 20).build());
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font,
                Component.literal("LAG PANIC / FORGE v0.5.0 / " + (performance ? "DIAGNOSTICS" : "MIDNIGHT")),
                width / 2, Math.max(7, height / 2 - 149), 0xFF77D3EF);
        var t = ForgeLagPanic.Client.telemetry();
        var d = ForgeLagPanic.Client.diagnostics();
        graphics.drawCenteredString(font, "Session: " + t.manualActivations() + " manual / "
                + t.autoActivations() + " auto | Active: "
                + t.activeSeconds(System.nanoTime()) + "s", width / 2,
                Math.min(height - 35, height / 2 + 118), 0xFF77D3EF);
        graphics.drawCenteredString(font, "Stalls: " + d.count() + " | Worst: "
                + d.worstPauseMillis() + "ms | Avg: " + d.averagePauseMillis()
                + "ms", width / 2, Math.min(height - 22, height / 2 + 131), 0xFF8795A8);
        graphics.drawCenteredString(font, "Last Panic trigger: " + t.lastTriggerLabel(),
                width / 2, Math.min(height - 9, height / 2 + 144), 0xFF8795A8);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
}
