package dev.zaan.lagpanic.ui;

import dev.zaan.lagpanic.Config;
import dev.zaan.lagpanic.LagPanicClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Compact two-column dark settings screen; all toggles persist immediately. */
public final class SettingsScreen extends Screen {
    private final Screen parent;
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
        int y = Math.max(32, height / 2 - 104);
        int row = 24;

        option(left, y, w, "Panic: " + (LagPanicClient.isActive() ? "ACTIVE" : "STANDBY"),
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

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(width / 2 - 70, y + row * 7 + 1, 140, 20).build());
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mx, int my, float partialTick) {
        super.extractRenderState(graphics, mx, my, partialTick);
        graphics.centeredText(font, Component.literal("LAG PANIC  /  MIDNIGHT"),
                width / 2, Math.max(14, height / 2 - 129), 0xFF77D3EF);
        graphics.centeredText(font, Component.literal("F8 = toggle  |  Change key in Minecraft controls"),
                width / 2, Math.min(height - 12, height / 2 + 99), 0xFF8795A8);
    }

    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
