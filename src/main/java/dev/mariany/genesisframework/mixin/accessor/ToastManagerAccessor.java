package dev.mariany.genesisframework.mixin.accessor;

import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ToastManager.class)
public interface ToastManagerAccessor {
    @Invoker("findFreeSlotsIndex")
    int genesis$findFreeSlotsIndex(int requiredCount);
}
