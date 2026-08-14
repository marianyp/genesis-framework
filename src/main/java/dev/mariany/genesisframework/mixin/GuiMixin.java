package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.event.client.gui.ClientGuiEvents;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    /**
     * Invokes {@link ClientGuiEvents#BEFORE_TOASTS_UPDATE} before the toast manager updates.
     */
    @Inject(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/ToastManager;update()V")
    )
    private void injectRender(CallbackInfo ci) {
        ClientGuiEvents.BEFORE_TOASTS_UPDATE.invoker().beforeToastsUpdate();
    }
}
