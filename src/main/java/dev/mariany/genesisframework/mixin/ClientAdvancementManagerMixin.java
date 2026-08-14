package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.event.client.advancement.ClientAdvancementEvents;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientAdvancements.class)
public class ClientAdvancementManagerMixin {
    /**
     * Invokes {@link ClientAdvancementEvents#UPDATED} after the client applies an advancement update.
     */
    @Inject(method = "update", at = @At("TAIL"))
    private void injectUpdate(ClientboundUpdateAdvancementsPacket packet, CallbackInfo ci) {
        ClientAdvancements advancements = (ClientAdvancements) (Object) this;
        ClientAdvancementEvents.UPDATED.invoker().onUpdated(advancements, packet);
    }
}
