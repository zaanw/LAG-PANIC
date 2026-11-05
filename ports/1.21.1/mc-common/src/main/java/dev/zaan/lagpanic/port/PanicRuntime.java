package dev.zaan.lagpanic.port;

import dev.zaan.lagpanic.Preset;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class PanicRuntime {
    private static Integer previousRenderDistance;
    private static Path recoveryFile;
    private static boolean recoveryChecked;
    private static boolean recoveryHealthy;

    private PanicRuntime() { }

    public static void initialize(Path configDirectory) {
        recoveryFile = configDirectory.resolve("lagpanic-1211-render-recovery.txt");
        recoveryChecked = false;
        recoveryHealthy = false;
    }

    public static void onClientTick(Minecraft client) {
        if (recoveryFile == null) return;
        if (!recoveryChecked) {
            recoveryChecked = true;
            recoveryHealthy = recoverFile(client);
        }
        if (previousRenderDistance != null && client.level == null) {
            disable(client);
        }
    }

    public static void toggle(Minecraft client) {
        if (!recoveryChecked || !recoveryHealthy || client.player == null || client.level == null) return;
        if (previousRenderDistance != null) {
            disable(client);
            return;
        }
        int previous = client.options.renderDistance().get();
        if (!writeBackup(previous)) {
            client.player.displayClientMessage(Component.literal("Lag Panic: backup failed; settings unchanged"), true);
            return;
        }
        previousRenderDistance = previous;
        int reduced = Math.min(previous, Preset.forName("Medium").renderDistance());
        client.options.renderDistance().set(reduced);
        client.options.save();
        client.player.displayClientMessage(Component.literal("Lag Panic: ON (render distance " + reduced + ")"), true);
    }

    private static void disable(Minecraft client) {
        int restore = previousRenderDistance;
        client.options.renderDistance().set(restore);
        client.options.save();
        try {
            Files.deleteIfExists(recoveryFile);
            previousRenderDistance = null;
        } catch (IOException ex) {
            System.err.println("[LagPanic] Cannot delete recovery file: " + ex);
            recoveryHealthy = false;
        }
        if (client.player != null)
            client.player.displayClientMessage(Component.literal("Lag Panic: OFF (graphics restored)"), true);
    }

    private static boolean writeBackup(int original) {
        Path tmp = recoveryFile.resolveSibling(recoveryFile.getFileName() + ".tmp");
        try {
            Files.createDirectories(recoveryFile.getParent());
            Files.writeString(tmp, Integer.toString(original), StandardCharsets.UTF_8);
            try {
                Files.move(tmp, recoveryFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, recoveryFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException ex) {
            System.err.println("[LagPanic] Cannot persist recovery file: " + ex);
            return false;
        }
    }

    private static boolean recoverFile(Minecraft client) {
        if (!Files.exists(recoveryFile)) return true;
        try {
            int original = Integer.parseInt(Files.readString(recoveryFile, StandardCharsets.UTF_8).trim());
            if (original < 2 || original > 64) throw new IOException("Invalid render distance: " + original);
            client.options.renderDistance().set(original);
            client.options.save();
            Files.delete(recoveryFile);
            return true;
        } catch (Exception ex) {
            System.err.println("[LagPanic] Recovery failed, keeping backup and disabling Panic: " + ex);
            return false;
        }
    }
}
