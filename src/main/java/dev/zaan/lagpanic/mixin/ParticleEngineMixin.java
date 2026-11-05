package dev.zaan.lagpanic.mixin;

import dev.zaan.lagpanic.LagPanicClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void lagpanic$skipExplosionParticles(
            ParticleOptions options, double x, double y, double z,
            double speedX, double speedY, double speedZ,
            CallbackInfoReturnable<Particle> cir) {
        if (!LagPanicClient.shouldHideExplosionParticles()) return;
        if (options.getType() == ParticleTypes.EXPLOSION
                || options.getType() == ParticleTypes.EXPLOSION_EMITTER
                || options.getType() == ParticleTypes.FLASH) {
            cir.setReturnValue(null);
        }
    }
}
