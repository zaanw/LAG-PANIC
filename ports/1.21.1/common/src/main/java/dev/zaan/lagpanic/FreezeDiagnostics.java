package dev.zaan.lagpanic;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

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

    public void sample(int latestTickGapMs, int heapPercent) {
        long gcTime = gcMillis();
        long gcDelta = previousGcMillis < 0 ? 0 : Math.max(0, gcTime - previousGcMillis);
        previousGcMillis = gcTime;
        if (latestTickGapMs < 750) return;
        pauseCount++;
        lastPauseMillis = latestTickGapMs;
        lastGcDeltaMillis = gcDelta;
        lastHeapPercent = Math.max(0, Math.min(100, heapPercent));
    }

    public long count() { return pauseCount; }
    public long lastPauseMillis() { return lastPauseMillis; }
    public long lastGcDeltaMillis() { return lastGcDeltaMillis; }
    public int lastHeapPercent() { return lastHeapPercent; }
}
