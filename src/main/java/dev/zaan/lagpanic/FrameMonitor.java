package dev.zaan.lagpanic;

public final class FrameMonitor {
    private static final int WINDOW = 120;
    private static final long MAX_GAP_NANOS = 1_000_000_000L;
    private final long[] samples = new long[WINDOW];
    private long lastFrameNanos;
    private long sumNanos;
    private int count;
    private int next;

    public void reset() {
        lastFrameNanos = 0L;
        sumNanos = 0L;
        count = 0;
        next = 0;
    }

    public void sample(long now) {
        if (lastFrameNanos == 0L) {
            lastFrameNanos = now;
            return;
        }
        long delta = now - lastFrameNanos;
        lastFrameNanos = now;
        if (delta <= 0 || delta > MAX_GAP_NANOS) {

            reset();
            lastFrameNanos = now;
            return;
        }
        if (count == WINDOW) sumNanos -= samples[next];
        else count++;
        samples[next] = delta;
        sumNanos += delta;
        next = (next + 1) % WINDOW;
    }

    public int sampleCount() { return count; }

    public int estimatedFps() {
        return sumNanos <= 0 ? 0 : (int) Math.min(999, 1_000_000_000.0 * count / sumNanos);
    }

    public boolean sustainedLowFps(int fpsThreshold) {
        if (count < WINDOW) return false;

        long interval = 1_000_000_000L / Math.max(1, fpsThreshold);
        int slow = 0;
        for (int i = 0; i < WINDOW; i++) {
            if (samples[i] > interval) slow++;
        }
        return slow >= 96 && (sumNanos / WINDOW) > interval;
    }
}
