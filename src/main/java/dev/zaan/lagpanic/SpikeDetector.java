package dev.zaan.lagpanic;

public final class SpikeDetector {
    private int spikes;
    private int lastGapMs;
    private long lastSpikeNanos;
    private long lastTickNanos;

    public void reset() {
        spikes = 0;
        lastGapMs = 0;
        lastSpikeNanos = 0;
        lastTickNanos = 0;
    }
    public int lastGapMs() { return lastGapMs; }

    public boolean recordTick(long now, int thresholdMs, int needed) {
        if (lastTickNanos == 0) {
            lastTickNanos = now;
            return false;
        }
        long elapsed = Math.max(0L, now - lastTickNanos);
        lastTickNanos = now;
        lastGapMs = (int)Math.min(30_000L, elapsed / 1_000_000L);
        if (lastGapMs < thresholdMs) return false;
        if (lastSpikeNanos == 0 || now - lastSpikeNanos > 15_000_000_000L)
            spikes = 0;
        lastSpikeNanos = now;
        return ++spikes >= needed;
    }
    public boolean quietFor(long now, int seconds) {
        return lastSpikeNanos == 0
                || now - lastSpikeNanos >= seconds * 1_000_000_000L;
    }
}
