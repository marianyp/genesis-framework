package dev.mariany.genesisframework.mixin.compat.jei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.event.client.jei.JEIRecipeTransferEvents;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Pseudo
@Mixin(targets = "mezz.jei.common.transfer.RecipeTransferService", remap = false)
public abstract class RecipeTransferServiceMixin {
    @WrapOperation(
            method = "transferRecipe(Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;Lmezz/jei/api/gui/IRecipeLayoutDrawable;Lnet/minecraft/world/entity/player/Player;ZZ)Ljava/util/Optional;",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/recipe/transfer/IRecipeTransferHandler;transferRecipe(Lmezz/jei/api/recipe/transfer/IRecipeTransferContext;Z)Lmezz/jei/api/recipe/transfer/IRecipeTransferError;"
            ),
            require = 1
    )
    private static <C extends AbstractContainerMenu, R> @Nullable IRecipeTransferError wrapTransferRecipe(
            IRecipeTransferHandler<C, R> transferHandler,
            IRecipeTransferContext<R, C> context,
            boolean doTransfer,
            Operation<IRecipeTransferError> original
    ) {
        Optional<IRecipeTransferError> optionalTransferError = JEIRecipeTransferEvents.GET_TRANSFER_ERROR
                .invoker()
                .getTransferError(context.getRecipeSlots());

        if (optionalTransferError.isEmpty()) {
            return original.call(transferHandler, context, doTransfer);
        }

        if (doTransfer) {
            context.completeRecipeTransfer(RecipeTransferResult.REJECTED);
        }

        return optionalTransferError.get();
    }
}
