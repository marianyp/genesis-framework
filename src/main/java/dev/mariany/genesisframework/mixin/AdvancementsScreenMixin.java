package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.event.client.advancement.ClientAdvancementEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenMixin {
    @Shadow
    @Final
    private Map<AdvancementHolder, AdvancementTab> tabs;

    @Shadow
    public abstract void onSelectedTabChanged(@Nullable AdvancementHolder advancement);

    @Shadow
    @Final
    private ClientAdvancements advancements;

    /**
     * Invokes {@link ClientAdvancementEvents#SCREEN_INITIALIZED} after the advancements screen initializes.
     */
    @Inject(method = "init", at = @At(value = "TAIL"))
    public void injectInit(CallbackInfo ci) {
        ClientAdvancementEvents.SCREEN_INITIALIZED
                .invoker()
                .onScreenInitialized(this.tabs, this.advancements, this::onSelectedTabChanged);
    }

    /**
     * Queries {@link ClientAdvancementEvents#ALLOW_ROOT} before adding an advancement root to the screen.
     */
    @WrapOperation(
            method = "onAdvancementsUpdated",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;create(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/advancements/AdvancementsScreen;ILnet/minecraft/advancements/AdvancementNode;)Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;"
            )
    )
    private @Nullable AdvancementTab wrapCreateTab(
            Minecraft minecraft,
            AdvancementsScreen screen,
            int index,
            AdvancementNode root,
            Operation<AdvancementTab> original
    ) {
        if (!ClientAdvancementEvents.ALLOW_ROOT.invoker().allowRoot(root)) {
            return null;
        }

        return original.call(minecraft, screen, index, root);
    }
}
