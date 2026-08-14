package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.event.server.entity.ServerEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PortalProcessor.class)
public class PortalProcessorMixin {
    /**
     * Modifies portal destinations through {@link ServerEntityEvents#MODIFY_PORTAL_DESTINATION}.
     */
    @Inject(method = "getPortalDestination", at = @At("RETURN"), cancellable = true)
    private void injectGetPortalDestination(
            ServerLevel serverLevel,
            Entity entity,
            CallbackInfoReturnable<TeleportTransition> cir
    ) {
        TeleportTransition originalDestination = cir.getReturnValue();

        TeleportTransition modifiedPortalDestination = ServerEntityEvents.MODIFY_PORTAL_DESTINATION
                .invoker()
                .modifyPortalDestination(entity, originalDestination);

        cir.setReturnValue(modifiedPortalDestination);
    }
}
