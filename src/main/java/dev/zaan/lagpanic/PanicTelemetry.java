package dev.zaan.lagpanic;

/**
 * In-memory session counters. No disk writes or network telemetry:
 * "telemetry" here means a local, private session diagnostic.
 */
public final class PanicTelemetry {
    public enum Trigger { MANUAL, LOW_FPS, TICK_SPIKES }

    private int manualActivations;
    private int autoActivations;
    private int lowFpsActivations;
    private int spikeActivations;
    private long totalActiveNanos;
    private long activatedAtNanos = -1L;
    private Trigger lastTrigger;

    public void activated(Trigger trigger, long nowNanos) {
        if (trigger == null) throw new IllegalArgumentException("trigger");
        if (activatedAtNanos >= 0) return;
        activatedAtNanos = Math.max(0L, nowNanos);
        lastTrigger = trigger;
        if (trigger == Trigger.MANUAL) manualActivations++;
        else {
            autoActivations++;
            if (trigger == Trigger.LOW_FPS) lowFpsActivations++;
            else spikeActivations++;
        }
    }

    public void deactivated(long nowNanos) {
        if (activatedAtNanos < 0) return;
        totalActiveNanos += Math.max(0L, nowNanos - activatedAtNanos);
        activatedAtNanos = -1L;
    }

    public int manualActivations() { return manualActivations; }
    public int autoActivations() { return autoActivations; }
    public int lowFpsActivations() { return lowFpsActivations; }
    public int spikeActivations() { return spikeActivations; }
    public Trigger lastTrigger() { return lastTrigger; }
    public boolean active() { return activatedAtNanos >= 0; }

    public long activeSeconds(long nowNanos) {
        long elapsed = active() ? Math.max(0L, nowNanos - activatedAtNanos) : 0L;
        return (totalActiveNanos + elapsed) / 1_000_000_000L;
    }

    public String lastTriggerLabel() {
        if (lastTrigger == null) return "NONE";
        return switch (lastTrigger) {
            case MANUAL -> "MANUAL";
            case LOW_FPS -> "LOW FPS";
            case TICK_SPIKES -> "TICK SPIKES";
        };
    }
}
