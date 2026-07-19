package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.client.instruction.ClientInstructionManager;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    /**
     * Update instruction toasts after updating ${@link net.minecraft.client.gui.components.toasts.ToastManager} toasts.
     */
    @Inject(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/ToastManager;update()V")
    )
    private void injectRender(CallbackInfo ci) {
        ClientInstructionManager.getInstance().update();
    }
}
