package dev.mariany.genesisframework.mixin.compat.jei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.event.client.jei.JEIRecipeTransferEvents;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "mezz.jei.common.transfer.RecipeTransferUtil", remap = false)
public abstract class RecipeTransferUtilMixin {
    @WrapOperation(
            method = "transferRecipe(Lmezz/jei/api/recipe/transfer/IRecipeTransferManager;Lnet/minecraft/world/inventory/AbstractContainerMenu;Lmezz/jei/api/gui/IRecipeLayoutDrawable;Lnet/minecraft/world/entity/player/Player;ZZ)Ljava/util/Optional;",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/recipe/transfer/IRecipeTransferHandler;transferRecipe(Lnet/minecraft/world/inventory/AbstractContainerMenu;Ljava/lang/Object;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/world/entity/player/Player;ZZ)Lmezz/jei/api/recipe/transfer/IRecipeTransferError;"
            ),
            require = 0
    )
    private static <C extends AbstractContainerMenu, R> @Nullable IRecipeTransferError wrapTransferRecipe(
            IRecipeTransferHandler<C, R> transferHandler,
            C container,
            R recipe,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer,
            Operation<IRecipeTransferError> original
    ) {
        return JEIRecipeTransferEvents.GET_TRANSFER_ERROR
                .invoker()
                .getTransferError(recipeSlots)
                .orElseGet(() -> original.call(
                        transferHandler,
                        container,
                        recipe,
                        recipeSlots,
                        player,
                        maxTransfer,
                        doTransfer
                ));
    }
}
