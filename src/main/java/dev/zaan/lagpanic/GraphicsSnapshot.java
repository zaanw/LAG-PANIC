package dev.zaan.lagpanic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.CloudStatus;
import net.minecraft.server.level.ParticleStatus;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Durable recovery record: if the game crashes in panic mode, original values
 * can be recovered at next startup. Never overwrites an option the user has
 * changed manually after panic was applied.
 */
public final class GraphicsSnapshot {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public int originalRender;
    public int appliedRender;
    public double originalEntities;
    public double appliedEntities;
    public String originalParticles;
    public String appliedParticles;
    public String originalClouds;
    public String appliedClouds;

    static Path backupPath() { return FabricLoader.getInstance().getConfigDir().resolve("lagpanic-recovery.json"); }

    public static GraphicsSnapshot capture(Options o) {
        GraphicsSnapshot s = new GraphicsSnapshot();
        s.originalRender = o.renderDistance().get();
        s.appliedRender = s.originalRender;
        s.originalEntities = o.entityDistanceScaling().get();
        s.appliedEntities = s.originalEntities;
        s.originalParticles = o.particles().get().name();
        s.appliedParticles = s.originalParticles;
        s.originalClouds = o.cloudStatus().get().name();
        s.appliedClouds = s.originalClouds;
        return s;
    }

    public void apply(Options o, Config c) {
        Preset preset = Preset.forName(c.profile);
        if (c.changeRenderDistance) {
            appliedRender = Math.min(originalRender, preset.renderDistance());
            o.renderDistance().set(appliedRender);
        }
        if (c.changeEntityDistance) {
            appliedEntities = Math.min(originalEntities, preset.entityDistance());
            o.entityDistanceScaling().set(appliedEntities);
        }
        if (c.changeParticles) {
            appliedParticles = "Light".equals(c.profile) ? ParticleStatus.DECREASED.name() : ParticleStatus.MINIMAL.name();
            o.particles().set(ParticleStatus.valueOf(appliedParticles));
        }
        if (c.changeClouds) {
            appliedClouds = "Light".equals(c.profile) ? CloudStatus.FAST.name() : CloudStatus.OFF.name();
            o.cloudStatus().set(CloudStatus.valueOf(appliedClouds));
        }
    }

    public void recover(Options o) {
        if (o.renderDistance().get() == appliedRender) o.renderDistance().set(originalRender);
        if (Math.abs(o.entityDistanceScaling().get() - appliedEntities) < 0.000001)
            o.entityDistanceScaling().set(originalEntities);
        if (o.particles().get().name().equals(appliedParticles))
            o.particles().set(ParticleStatus.valueOf(originalParticles));
        if (o.cloudStatus().get().name().equals(appliedClouds))
            o.cloudStatus().set(CloudStatus.valueOf(originalClouds));
    }

    public void persist() {
        try {
            Path p = backupPath();
            Files.createDirectories(p.getParent());
            Files.writeString(p, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (Exception e) { System.err.println("[LagPanic] Recovery backup failed: " + e); }
    }

    public static boolean restorePending(Minecraft mc) {
        Path p = backupPath();
        if (!Files.exists(p)) return false;
        try {
            GraphicsSnapshot s = GSON.fromJson(Files.readString(p, StandardCharsets.UTF_8), GraphicsSnapshot.class);
            if (s == null) return false;
            s.recover(mc.options);
            mc.options.save();
            Files.deleteIfExists(p);
            return true;
        } catch (Exception e) {
            System.err.println("[LagPanic] Could not recover graphics; backup retained: " + e);
            return false;
        }
    }

    public static void clearBackup() {
        try { Files.deleteIfExists(backupPath()); }
        catch (Exception e) { System.err.println("[LagPanic] Backup cleanup failed: " + e); }
    }
}
