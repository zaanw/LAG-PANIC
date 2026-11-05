package dev.zaan.lagpanic;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * Lightweight diagnostic snapshots: counts long client-tick pauses.
 * GC time is only correlated, not proof of the stall's cause.
 */
public final class FreezeDiagnostics {
    private static final List<GarbageCollectorMXBean> GC_BEANS =
            ManagementFactory.getGarbageCollectorMXBeans();
    private long previousGcMillis = -1L;
    private long lastPauseMillis;
    private long lastGcDeltaMillis;
    private int lastHeapPercent;
    private long pauseCount;

    private static long gcMillis() {
        long sum = 0L;
        for (GarbageCollectorMXBean bean : GC_BEANS) {
            long t = bean.getCollectionTime();
            if (t > 0) sum += t;
        }
        return sum;
    }

    /** Called from the main tick only once per ~20 ticks, plus on long stalls. */
    public void sample(int latestTickGapMs, MemoryPressure.Heap heap) {
        long gcTime = gcMillis();
        long gcDelta = previousGcMillis < 0 ? 0 : Math.max(0, gcTime - previousGcMillis);
        previousGcMillis = gcTime;
        if (latestTickGapMs < 750) return;
        pauseCount++;
        lastPauseMillis = latestTickGapMs;
        lastGcDeltaMillis = gcDelta;
        lastHeapPercent = heap.percent();
    }

    public long count() { return pauseCount; }
    public long lastPauseMillis() { return lastPauseMillis; }
    public long lastGcDeltaMillis() { return lastGcDeltaMillis; }
    public int lastHeapPercent() { return lastHeapPercent; }
}
