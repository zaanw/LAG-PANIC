package dev.zaan.lagpanic;

/** Input: elapsed time between client ticks, not actual frame time. */
public final class SpikeDetector {
    private int spikes;
    private long lastSpikeNanos;
    private long lastTickNanos;

    public void reset() { spikes = 0; lastSpikeNanos = 0; lastTickNanos = 0; }
    public boolean recordTick(long now, int thresholdMs, int needed) {
        if (lastTickNanos == 0) { lastTickNanos = now; return false; }
        long elapsed = now - lastTickNanos;
        lastTickNanos = now;
        if (elapsed < thresholdMs * 1_000_000L) return false;
        if (lastSpikeNanos == 0 || now - lastSpikeNanos > 15_000_000_000L) spikes = 0;
        lastSpikeNanos = now;
        return ++spikes >= needed;
    }
    public boolean quietFor(long now, int seconds) {
        return lastSpikeNanos == 0 || now - lastSpikeNanos >= seconds * 1_000_000_000L;
    }
}
