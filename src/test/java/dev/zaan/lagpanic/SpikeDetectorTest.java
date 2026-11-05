package dev.zaan.lagpanic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpikeDetectorTest {
    @Test void repeatedSpikesTrigger() {
        SpikeDetector d = new SpikeDetector();
        long n = 1_000_000_000L;
        assertFalse(d.recordTick(n, 200, 3));
        assertFalse(d.recordTick(n + 300_000_000L, 200, 3));
        assertFalse(d.recordTick(n + 600_000_000L, 200, 3));
        assertTrue(d.recordTick(n + 900_000_000L, 200, 3));
        assertEquals(300, d.lastGapMs());
        assertFalse(d.quietFor(n + 2_000_000_000L, 10));
        assertTrue(d.quietFor(n + 20_000_000_000L, 10));
    }

    @Test void spikeCounterExpiresAfterQuietWindow() {
        SpikeDetector d = new SpikeDetector();
        long n = 1_000_000_000L;
        d.recordTick(n, 200, 2);
        assertFalse(d.recordTick(n + 300_000_000L, 200, 2));
        assertFalse(d.recordTick(n + 20_000_000_000L, 200, 2));
        assertTrue(d.recordTick(n + 20_300_000_000L, 200, 2));
        d.reset();
        assertEquals(0, d.lastGapMs());
    }

    @Test void configurableLimitsAreClamped() {
        Config c = new Config();
        c.spikeThresholdMs = -100;
        c.spikesToTrigger = 0;
        c.stageIntervalTicks = 0;
        c.autoCooldownSeconds = 10;
        c.stableSecondsToRestore = 1;
        c.profile = "unknown";
        c.validate();
        assertEquals(125, c.spikeThresholdMs);
        assertEquals(2, c.spikesToTrigger);
        assertEquals(1, c.stageIntervalTicks);
        assertEquals(30, c.autoCooldownSeconds);
        assertEquals(20, c.stableSecondsToRestore);
        assertEquals("Medium", c.profile);
    }

    @Test void presetValues() {
        assertEquals(4, Preset.forName("Nuclear").renderDistance());
        assertEquals(8, Preset.forName("Medium").renderDistance());
        assertEquals(12, Preset.forName("Light").renderDistance());
    }
}
