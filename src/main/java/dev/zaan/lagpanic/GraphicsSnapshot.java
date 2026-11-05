package dev.zaan.lagpanic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.server.level.ParticleStatus;

/**
 * Captures exactly the settings we might change. The recovery file containing
 * intended targets MUST be committed BEFORE the first graphics modification.
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

    static Path backupPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("lagpanic-recovery.json");
    }
    public static boolean hasPendingBackup() { return Files.exists(backupPath()); }

    public static GraphicsSnapshot prepare(Options o, Config c) {
        GraphicsSnapshot s = new GraphicsSnapshot();
        s.originalRender = o.renderDistance().get();
        s.originalEntities = o.entityDistanceScaling().get();
        s.originalParticles = o.particles().get().name();
        s.originalClouds = o.cloudStatus().get().name();
        Preset p = Preset.forName(c.profile);
        s.appliedRender = c.changeRenderDistance
                ? Math.min(s.originalRender, p.renderDistance()) : s.originalRender;
        s.appliedEntities = c.changeEntityDistance
                ? Math.min(s.originalEntities, p.entityDistance()) : s.originalEntities;

        // Never upgrade MINIMAL to DECREASED or clouds OFF to FAST.
        ParticleStatus particles = o.particles().get();
        ParticleStatus particleTarget = "Light".equals(c.profile)
                ? ParticleStatus.DECREASED : ParticleStatus.MINIMAL;
        s.appliedParticles = c.changeParticles && particles.ordinal() < particleTarget.ordinal()
                ? particleTarget.name() : particles.name();

        CloudStatus clouds = o.cloudStatus().get();
        CloudStatus cloudTarget = "Light".equals(c.profile)
                ? CloudStatus.FAST : CloudStatus.OFF;
        s.appliedClouds = c.changeClouds && clouds.ordinal() > cloudTarget.ordinal()
                ? cloudTarget.name() : clouds.name();
        return s;
    }

    /**
     * One option per invocation, spread over multiple ticks to avoid applying
     * all costly changes simultaneously. A manual change made before this step
     * is respected.
     */
    public void applyStep(Options o, int step) {
        switch (step) {
            case 0 -> {
                if (!originalParticles.equals(appliedParticles)
                        && o.particles().get().name().equals(originalParticles))
                    o.particles().set(ParticleStatus.valueOf(appliedParticles));
            }
            case 1 -> {
                if (!originalClouds.equals(appliedClouds)
                        && o.cloudStatus().get().name().equals(originalClouds))
                    o.cloudStatus().set(CloudStatus.valueOf(appliedClouds));
            }
            case 2 -> {
                if (originalEntities != appliedEntities
                        && Math.abs(o.entityDistanceScaling().get() - originalEntities) < 0.000001)
                    o.entityDistanceScaling().set(appliedEntities);
            }
            case 3 -> {
                if (originalRender != appliedRender && o.renderDistance().get() == originalRender)
                    o.renderDistance().set(appliedRender);
            }
            default -> throw new IllegalArgumentException("Unknown application step: " + step);
        }
    }

    /**
     * Restore only values that still equal the ones Lag Panic applied.
     * Unchanged options are NEVER restored (otherwise we'd overwrite the
     * player's own changes while Panic was active).
     */
    public void recover(Options o) {
        if (originalRender != appliedRender && o.renderDistance().get() == appliedRender)
            o.renderDistance().set(originalRender);
        if (originalEntities != appliedEntities
                && Math.abs(o.entityDistanceScaling().get() - appliedEntities) < 0.000001)
            o.entityDistanceScaling().set(originalEntities);
        if (!originalParticles.equals(appliedParticles)
                && o.particles().get().name().equals(appliedParticles))
            o.particles().set(ParticleStatus.valueOf(originalParticles));
        if (!originalClouds.equals(appliedClouds)
                && o.cloudStatus().get().name().equals(appliedClouds))
            o.cloudStatus().set(CloudStatus.valueOf(originalClouds));
    }

    /** Fail closed: if persistence fails, DO NOT change any graphics option. */
    public boolean persist() {
        Path p = backupPath();
        Path tmp = p.resolveSibling(p.getFileName() + ".tmp");
        try {
            Files.createDirectories(p.getParent());
            Files.writeString(tmp, GSON.toJson(this), StandardCharsets.UTF_8);
            try {
                Files.move(tmp, p, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            System.err.println("[LagPanic] Cannot write backup, refusing to activate: " + e);
            return false;
        }
    }

    public static boolean restorePending(Minecraft mc) {
        Path p = backupPath();
        if (!Files.exists(p)) return true;
        try {
            GraphicsSnapshot s = GSON.fromJson(Files.readString(p, StandardCharsets.UTF_8), GraphicsSnapshot.class);
            if (s == null || s.originalClouds == null || s.appliedClouds == null
                    || s.originalParticles == null || s.appliedParticles == null)
                throw new IOException("Invalid recovery data");
            s.recover(mc.options);
            mc.options.save();
            Files.delete(p);
            return true;
        } catch (Exception e) {
            System.err.println("[LagPanic] Recovery failed; backup kept, activation blocked: " + e);
            return false;
        }
    }

    public static boolean clearBackup() {
        try { Files.deleteIfExists(backupPath()); return true; }
        catch (IOException e) {
            System.err.println("[LagPanic] Failed deleting backup; recovery will retry on next startup: " + e);
            return false;
        }
    }
}
