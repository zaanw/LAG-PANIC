package dev.zaan.lagpanic.mixin;

import dev.zaan.lagpanic.LagPanicClient;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void lagpanic$skipPrimedTnt(Entity entity, Frustum frustum,
                                        double cameraX, double cameraY, double cameraZ,
                                        float partialTick, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof PrimedTnt && LagPanicClient.shouldHidePrimedTnt()) {
            cir.setReturnValue(false);
        }
    }
}
