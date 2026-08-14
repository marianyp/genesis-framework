package dev.mariany.genesisframework.compat.jei;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.compat.AgeRecipeTransferMessages;
import dev.mariany.genesisframework.event.client.jei.JEIRecipeTransferEvents;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class JEIAgeTransferHandler {
    private JEIAgeTransferHandler() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("JEI Age Transfer Handler");
        JEIRecipeTransferEvents.GET_TRANSFER_ERROR.register(JEIAgeTransferHandler::getTransferError);
    }

    private static Optional<IRecipeTransferError> getTransferError(IRecipeSlotsView recipeSlots) {
        List<ItemStack> outputs = recipeSlots
                .getSlotViews(RecipeIngredientRole.OUTPUT)
                .stream()
                .flatMap(IRecipeSlotView::getItemStacks)
                .toList();

        return AgeRecipeTransferMessages.forLockedOutputs(outputs).map(AgeLockedError::new);
    }

    private record AgeLockedError(Component message) implements IRecipeTransferError {
        @Override
        public Type getType() {
            return Type.USER_FACING;
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltip) {
            tooltip.add(Component.translatable("jei.tooltip.transfer"));
            tooltip.add(this.message);
        }
    }
}
