package dev.mariany.genesisframework.event.client.jei;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class JEIRecipeTransferEvents {
    private JEIRecipeTransferEvents() {
    }

    public static final Event<GetTransferError> GET_TRANSFER_ERROR = EventFactory.createArrayBacked(
            GetTransferError.class,
            callbacks -> recipeSlots -> {
                for (GetTransferError callback : callbacks) {
                    Optional<IRecipeTransferError> error = callback.getTransferError(recipeSlots);

                    if (error.isPresent()) {
                        return error;
                    }
                }

                return Optional.empty();
            }
    );

    public interface GetTransferError {
        Optional<IRecipeTransferError> getTransferError(IRecipeSlotsView recipeSlots);
    }
}
