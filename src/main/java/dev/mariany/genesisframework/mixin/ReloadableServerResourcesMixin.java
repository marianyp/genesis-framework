package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.advancement.ServerAdvancementManagerReloadListener;
import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
    @Shadow
    @Final
    private ServerAdvancementManager advancements;

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/server/ServerAdvancementManager;"
            )
    )
    private ServerAdvancementManager createAdvancementManager(
            HolderLookup.Provider registries,
            Operation<ServerAdvancementManager> original
    ) {
        return ServerAdvancementEvents.CREATE_MANAGER.invoker().createManager(
                registries,
                original::call
        );
    }

    @ModifyReturnValue(method = "listeners", at = @At("RETURN"))
    private List<PreparableReloadListener> addAdvancementLoadListener(List<PreparableReloadListener> original) {
        List<PreparableReloadListener> listeners = new ArrayList<>(original);
        listeners.add(new ServerAdvancementManagerReloadListener(this.advancements));
        return List.copyOf(listeners);
    }
}
