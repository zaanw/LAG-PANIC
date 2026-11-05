package dev.zaan.lagpanic.ui;

import dev.zaan.lagpanic.Config;
import dev.zaan.lagpanic.LagPanicClient;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SettingsScreen extends Screen {
    private final Screen parent;
    private final Config c = LagPanicClient.config;

    public SettingsScreen(Screen parent) {
        super(Component.literal("Lag Panic"));
        this.parent = parent;
    }

    @Override protected void init() {
        int x = width / 2 - 115, y = height / 2 - 126, w = 230;
        addRenderableWidget(Button.builder(Component.literal("Panic mode: " + (LagPanicClient.isActive() ? "ACTIVE" : "STANDBY")), b -> {
            LagPanicClient.toggle(minecraft); rebuildWidgets();
        }).bounds(x, y, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Profile: " + c.profile), b -> {
            c.profile = switch (c.profile) {
                case "Light" -> "Medium";
                case "Medium" -> "Nuclear";
                default -> "Light";
            };
            c.save(); rebuildWidgets();
        }).bounds(x, y + 26, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Auto detection: " + (c.auto ? "ON" : "OFF")), b -> {
            c.auto = !c.auto; c.save(); rebuildWidgets();
        }).bounds(x, y + 52, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Auto restore: " + (c.restoreAutomatically ? "ON" : "OFF")), b -> {
            c.restoreAutomatically = !c.restoreAutomatically; c.save(); rebuildWidgets();
        }).bounds(x, y + 78, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Render distance: " + on(c.changeRenderDistance)), b -> {
            c.changeRenderDistance = !c.changeRenderDistance; c.save(); rebuildWidgets();
        }).bounds(x, y + 104, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Particles: " + on(c.changeParticles)), b -> {
            c.changeParticles = !c.changeParticles; c.save(); rebuildWidgets();
        }).bounds(x, y + 130, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Entity distance: " + on(c.changeEntityDistance)), b -> {
            c.changeEntityDistance = !c.changeEntityDistance; c.save(); rebuildWidgets();
        }).bounds(x, y + 156, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Clouds: " + on(c.changeClouds)), b -> {
            c.changeClouds = !c.changeClouds; c.save(); rebuildWidgets();
        }).bounds(x, y + 182, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(x, y + 208, w, 20).build());
    }
    private static String on(boolean b) { return b ? "ON" : "OFF"; }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
