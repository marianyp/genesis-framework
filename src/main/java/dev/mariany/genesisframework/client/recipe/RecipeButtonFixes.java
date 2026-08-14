package dev.mariany.genesisframework.client.recipe;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.event.client.recipe.ClientRecipeEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class RecipeButtonFixes {
    private RecipeButtonFixes() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Recipe Button Fixes");
        ClientRecipeEvents.MODIFY_BUTTON_ENTRIES.register(RecipeButtonFixes::withoutEmptyDisplayItems);
    }

    private static List<RecipeButton.ResolvedEntry> withoutEmptyDisplayItems(List<RecipeButton.ResolvedEntry> entries) {
        return entries.stream().filter(entry -> !entry.displayItems().isEmpty()).toList();
    }
}
