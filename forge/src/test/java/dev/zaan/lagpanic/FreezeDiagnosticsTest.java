package dev.zaan.lagpanic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FreezeDiagnosticsTest {
    @Test void recordsOnlyLongStallsAndMaintainsSummary() {
        FreezeDiagnostics diagnostics = new FreezeDiagnostics();
        MemoryPressure.Heap heap = new MemoryPressure.Heap(300, 512, 1024, 30);
        diagnostics.sample(100, heap);
        diagnostics.sample(749, heap);
        assertEquals(0, diagnostics.count());
        diagnostics.sample(750, heap);
        diagnostics.sample(1500, heap);
        diagnostics.sample(1000, heap);
        assertEquals(3, diagnostics.count());
        assertEquals(1500, diagnostics.worstPauseMillis());
        assertEquals(1083, diagnostics.averagePauseMillis());
        assertEquals(1000, diagnostics.lastPauseMillis());
        assertEquals(30, diagnostics.lastHeapPercent());
        diagnostics.sample(120, heap);
        assertEquals(3, diagnostics.count());
        assertEquals(1500, diagnostics.worstPauseMillis());
    }
}
