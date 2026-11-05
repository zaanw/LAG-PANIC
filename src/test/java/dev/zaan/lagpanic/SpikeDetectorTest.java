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
        assertFalse(d.quietFor(n + 2_000_000_000L, 10));
        assertTrue(d.quietFor(n + 20_000_000_000L, 10));
    }
    @Test void presetValues() {
        assertEquals(4, Preset.forName("Nuclear").renderDistance());
        assertEquals(8, Preset.forName("Medium").renderDistance());
        assertEquals(12, Preset.forName("Light").renderDistance());
    }
}
