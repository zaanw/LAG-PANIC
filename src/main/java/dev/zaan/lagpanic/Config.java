package dev.zaan.lagpanic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import net.fabricmc.loader.api.FabricLoader;

/** User preferences; no vanilla graphics options are permanently overwritten. */
public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean auto = false;  // deliberate opt-in, auto switching may cause chunk rebuild stalls
    public boolean restoreAutomatically = true;
    public boolean changeRenderDistance = true;
    public boolean changeParticles = true;
    public boolean changeEntityDistance = true;
    public boolean changeClouds = true;
    public String profile = "Medium";
    public int spikeThresholdMs = 250;
    public int spikesToTrigger = 3;
    public int stableSecondsToRestore = 45;

    public void validate() {
        if (!"Light".equals(profile) && !"Medium".equals(profile) && !"Nuclear".equals(profile)) profile = "Medium";
        spikeThresholdMs = Math.max(125, Math.min(2000, spikeThresholdMs));
        spikesToTrigger = Math.max(2, Math.min(10, spikesToTrigger));
        stableSecondsToRestore = Math.max(20, Math.min(300, stableSecondsToRestore));
    }
    static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("lagpanic.json"); }
    public static Config load() {
        try (var reader = Files.newBufferedReader(path(), StandardCharsets.UTF_8)) {
            Config c = GSON.fromJson(reader, Config.class);
            if (c != null) { c.validate(); return c; }
        } catch (Exception ignored) { }
        return new Config();
    }
    public void save() {
        validate();
        try {
            Files.createDirectories(path().getParent());
            try (var writer = Files.newBufferedWriter(path(), StandardCharsets.UTF_8)) { GSON.toJson(this, writer); }
        } catch (Exception e) { System.err.println("[LagPanic] Could not save settings: " + e); }
    }
}
