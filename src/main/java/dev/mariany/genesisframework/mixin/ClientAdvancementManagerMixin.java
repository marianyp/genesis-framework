package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.client.instruction.ClientInstructionManager;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientAdvancements.class)
public class ClientAdvancementManagerMixin {
    /**
     * Refresh instruction toasts during advancement update (i.e. data pack reload, advancement progress, etc.).
     */
    @Inject(method = "update", at = @At("TAIL"))
    private void onAdvancementUpdate(ClientboundUpdateAdvancementsPacket packet, CallbackInfo ci) {
        ClientInstructionManager.getInstance().refreshInstructionToasts();
    }
}
