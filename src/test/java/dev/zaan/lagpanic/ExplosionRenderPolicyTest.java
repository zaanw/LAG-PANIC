package dev.zaan.lagpanic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExplosionRenderPolicyTest {
    @Test void onlyActivePanicChangesTntRendering() {
        Config c = new Config();
        c.profile = "Nuclear";
        assertFalse(ExplosionRenderPolicy.hidePrimedTnt(false, c));
        assertFalse(ExplosionRenderPolicy.hideExplosionParticles(false, c));
        assertTrue(ExplosionRenderPolicy.hidePrimedTnt(true, c));
        assertTrue(ExplosionRenderPolicy.hideExplosionParticles(true, c));
    }
    @Test void nuclearGuardIsOptOut() {
        Config c = new Config();
        c.profile = "Nuclear";
        c.nuclearExplosionGuard = false;
        assertFalse(ExplosionRenderPolicy.hidePrimedTnt(true, c));
        assertFalse(ExplosionRenderPolicy.hideExplosionParticles(true, c));
        c.hidePrimedTnt = true;
        assertTrue(ExplosionRenderPolicy.hidePrimedTnt(true, c));
    }
    @Test void nonNuclearPreservesIndividualToggles() {
        Config c = new Config();
        c.profile = "Medium";
        assertFalse(ExplosionRenderPolicy.hidePrimedTnt(true, c));
        assertFalse(ExplosionRenderPolicy.hideExplosionParticles(true, c));
        c.hideExplosionParticles = true;
        assertTrue(ExplosionRenderPolicy.hideExplosionParticles(true, c));
        assertFalse(ExplosionRenderPolicy.hidePrimedTnt(true, c));
    }
}
