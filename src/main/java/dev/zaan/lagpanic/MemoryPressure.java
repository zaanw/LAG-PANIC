package dev.zaan.lagpanic;

public final class MemoryPressure {
    private static final long MIB = 1024L * 1024L;
    private static final long MIN_GC_INTERVAL_NANOS = 300_000_000_000L;
    private long lastRequestNanos = Long.MIN_VALUE;
    private boolean requested;

    public record Heap(long usedMiB, long committedMiB, long maxMiB, int percent) { }

    public static Heap sample() {
        Runtime r = Runtime.getRuntime();
        long committed = r.totalMemory();
        long used = Math.max(0, committed - r.freeMemory());
        long max = r.maxMemory();
        int percent = max > 0 ? (int)Math.min(100, used * 100.0 / max) : 0;
        return new Heap(used / MIB, committed / MIB, max / MIB, percent);
    }

    public boolean tryRequest(Config config, boolean automatically, long now) {
        if (!config.allowExperimentalGc || automatically) return false;
        if (sample().percent() < config.gcThresholdPercent) return false;
        if (requested && now - lastRequestNanos < MIN_GC_INTERVAL_NANOS) return false;
        requested = true;
        lastRequestNanos = now;
        try {

            Thread worker = new Thread(System::gc, "LagPanic-Optional-GC");
            worker.setDaemon(true);
            worker.start();
            return true;
        } catch (RuntimeException e) {
            System.err.println("[LagPanic] Optional GC request failed: " + e);
            return false;
        }
    }

    public static int clampThreshold(int value) { return Math.max(70, Math.min(95, value)); }
}
