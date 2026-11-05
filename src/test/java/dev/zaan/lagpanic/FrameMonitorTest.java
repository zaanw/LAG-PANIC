package dev.zaan.lagpanic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FrameMonitorTest {
    @Test void fastFramesNeverTrigger() {
        FrameMonitor m = new FrameMonitor();
        long t = 1_000_000_000L;
        for (int i = 0; i < 200; i++) {
            t += 10_000_000L; // ~100 FPS
            m.sample(t);
        }
        assertTrue(m.estimatedFps() >= 95);
        assertFalse(m.sustainedLowFps(35));
    }
    @Test void sustainedSlownessTriggers() {
        FrameMonitor m = new FrameMonitor();
        long t = 1_000_000_000L;
        for (int i = 0; i < 140; i++) {
            t += 50_000_000L; // ~20 FPS
            m.sample(t);
        }
        assertEquals(20, m.estimatedFps());
        assertTrue(m.sustainedLowFps(35));
        assertFalse(m.sustainedLowFps(15));
    }
    @Test void isolatedStallDoesNotTriggerAndResetPreventsMenuFalsePositive() {
        FrameMonitor m = new FrameMonitor();
        long t = 1_000_000_000L;
        for (int i = 0; i < 130; i++) {
            t += 10_000_000L;
            m.sample(t);
        }
        m.sample(t + 2_000_000_000L); // alt-tab or a large single freeze
        assertEquals(0, m.sampleCount());
        assertFalse(m.sustainedLowFps(35));
        for (int i = 0; i < 20; i++) {
            t += 100_000_000L;
            m.sample(t + 2_000_000_000L);
        }
        assertFalse(m.sustainedLowFps(35));
    }
    @Test void circularBufferDropsOldLowFpsPeriods() {
        FrameMonitor m = new FrameMonitor();
        long t = 1_000_000_000L;
        for (int i = 0; i < 130; i++) {
            t += 50_000_000L;
            m.sample(t);
        }
        assertTrue(m.sustainedLowFps(35));
        for (int i = 0; i < 160; i++) {
            t += 10_000_000L;
            m.sample(t);
        }
        assertFalse(m.sustainedLowFps(35));
        assertEquals(120, m.sampleCount());
    }
}
