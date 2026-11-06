package dev.zaan.lagpanic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PanicTelemetryTest {
    @Test void countsManualAndAutomaticTriggersSeparately() {
        PanicTelemetry t = new PanicTelemetry();
        t.activated(PanicTelemetry.Trigger.MANUAL, 1_000_000_000L);
        t.activated(PanicTelemetry.Trigger.LOW_FPS, 2_000_000_000L); // ignore duplicate activation
        assertEquals(1, t.manualActivations());
        assertEquals(0, t.autoActivations());
        assertEquals("MANUAL", t.lastTriggerLabel());
        t.deactivated(4_000_000_000L);
        t.activated(PanicTelemetry.Trigger.LOW_FPS, 8_000_000_000L);
        t.deactivated(10_000_000_000L);
        t.activated(PanicTelemetry.Trigger.TICK_SPIKES, 12_000_000_000L);
        assertEquals(1, t.manualActivations());
        assertEquals(2, t.autoActivations());
        assertEquals(1, t.lowFpsActivations());
        assertEquals(1, t.spikeActivations());
        assertEquals("TICK SPIKES", t.lastTriggerLabel());
        assertEquals(7, t.activeSeconds(14_000_000_000L));
    }

    @Test void ignoresDuplicateDeactivationAndClockBackwards() {
        PanicTelemetry t = new PanicTelemetry();
        assertEquals("NONE", t.lastTriggerLabel());
        t.deactivated(2_000_000_000L);
        t.activated(PanicTelemetry.Trigger.MANUAL, 10_000_000_000L);
        assertEquals(0, t.activeSeconds(9_000_000_000L));
        t.deactivated(9_000_000_000L);
        t.deactivated(200_000_000_000L);
        assertFalse(t.active());
        assertEquals(0, t.activeSeconds(300_000_000_000L));
    }
}
