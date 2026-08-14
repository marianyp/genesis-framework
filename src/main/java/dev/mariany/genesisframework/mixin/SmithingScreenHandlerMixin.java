package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.event.server.recipe.ServerRecipeEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(SmithingMenu.class)
public abstract class SmithingScreenHandlerMixin extends ItemCombinerMenu {
    public SmithingScreenHandlerMixin(
            @Nullable MenuType<?> type,
            int syncId,
            Inventory playerInventory,
            ContainerLevelAccess context,
            ItemCombinerMenuSlotDefinition forgingSlotsManager
    ) {
        super(type, syncId, playerInventory, context, forgingSlotsManager);
    }

    @Shadow
    protected abstract SmithingRecipeInput createRecipeInput();

    /**
     * Queries {@link ServerRecipeEvents#ALLOW_SMITHING} before creating a smithing result.
     */
    @Inject(method = "createResult", at = @At(value = "HEAD"), cancellable = true)
    public void injectUpdateResult(CallbackInfo ci) {
        if (!(this.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (ServerRecipeEvents.ALLOW_SMITHING.invoker().allowSmithing(serverPlayer, this.createRecipeInput())) {
            return;
        }

        ci.cancel();
    }
}
