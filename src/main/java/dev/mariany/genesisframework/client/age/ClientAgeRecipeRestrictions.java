package dev.mariany.genesisframework.client.age;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.client.GenesisFrameworkClient;
import dev.mariany.genesisframework.event.client.recipe.ClientRecipeEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class ClientAgeRecipeRestrictions {
    private ClientAgeRecipeRestrictions() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Client Age Recipe Restrictions");
        ClientRecipeEvents.MODIFY_DISPLAY_ITEMS.register(ClientAgeRecipeRestrictions::onModifyDisplayItems);
        ClientRecipeEvents.MODIFY_COLLECTIONS.register(ClientAgeRecipeRestrictions::onModifyCollections);
        ClientRecipeEvents.ALLOW_TOAST_RENDER.register(ClientAgeRecipeRestrictions::onAllowToastRender);
        ClientRecipeEvents.ALLOW_TOAST.register(ClientAgeRecipeRestrictions::onAllowToast);
    }

    private static List<ItemStack> onModifyDisplayItems(List<ItemStack> items) {
        List<ItemStack> filtered = null;

        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);

            if (GenesisFrameworkClient.getAgeManager().isUnlocked(item)) {
                if (filtered != null) {
                    filtered.add(item);
                }
            } else if (filtered == null) {
                filtered = new ArrayList<>(items.size() - 1);

                for (int j = 0; j < i; j++) {
                    filtered.add(items.get(j));
                }
            }
        }

        return filtered == null ? items : filtered;
    }

    private static List<RecipeCollection> onModifyCollections(List<RecipeCollection> collections, Level level) {
        ContextMap context = SlotDisplayContext.fromLevel(level);

        List<RecipeCollection> filtered = null;

        for (int i = 0; i < collections.size(); i++) {
            RecipeCollection collection = collections.get(i);

            if (hasUnlockedResult(collection, context)) {
                if (filtered != null) {
                    filtered.add(collection);
                }
            } else if (filtered == null) {
                filtered = new ArrayList<>(collections.size() - 1);

                for (int j = 0; j < i; j++) {
                    filtered.add(collections.get(j));
                }
            }
        }

        return filtered == null ? collections : filtered;
    }

    private static boolean hasUnlockedResult(RecipeCollection collection, ContextMap context) {
        for (RecipeDisplayEntry recipe : collection.getRecipes()) {
            List<ItemStack> results = recipe.resultItems(context);

            if (!results.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private static boolean onAllowToastRender(List<RecipeToast.Entry> recipeItems) {
        return !recipeItems.isEmpty();
    }

    private static boolean onAllowToast(ItemStack unlockedItem) {
        return GenesisFrameworkClient.getAgeManager().isUnlocked(unlockedItem);
    }
}