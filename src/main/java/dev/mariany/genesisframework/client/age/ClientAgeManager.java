package dev.mariany.genesisframework.client.age;

import dev.mariany.genesisframework.GenesisFramework;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.RecipeToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ClientAgeManager {
    private static final ClientAgeManager INSTANCE = new ClientAgeManager();

    private final List<Ingredient> lockedItems = new ArrayList<>();
    private boolean initiatedLockedItems = false;

    private ClientAgeManager() {
    }

    public static ClientAgeManager getInstance() {
        return INSTANCE;
    }

    public void reset() {
        GenesisFramework.LOGGER.info("Resetting Client Age Manager");

        this.lockedItems.clear();
        this.initiatedLockedItems = false;
    }

    public boolean isUnlocked(ItemStack stack) {
        return this.lockedItems.stream().noneMatch(ingredient -> ingredient.test(stack));
    }

    public void updateLockedItems(Collection<Ingredient> changes) {
        boolean initial = !this.initiatedLockedItems;
        int oldSize = this.lockedItems.size();
        List<Ingredient> difference = getDifference(this.lockedItems, changes);

        this.lockedItems.clear();
        this.lockedItems.addAll(changes);
        this.initiatedLockedItems = true;

        GenesisFramework.LOGGER.info(
                "Updated age instructions. Old Size: {} | New Size: {}",
                oldSize,
                this.lockedItems.size()
        );

        if (!initial) {
            afterUpdateItemUnlocks(difference);
        }
    }

    private static List<Ingredient> getDifference(
            Collection<Ingredient> before,
            Collection<Ingredient> after
    ) {
        Set<Ingredient> ingredients = new HashSet<>(after);

        return before.stream()
                     .filter(oldIngredient -> !ingredients.contains(oldIngredient))
                     .toList();
    }

    private void afterUpdateItemUnlocks(Collection<Ingredient> changes) {
        MinecraftClient client = MinecraftClient.getInstance();
        ToastManager toastManager = client.getToastManager();

        for (Ingredient ingredient : changes) {
            ingredient.getMatchingItems()
                      .forEach(entry -> RecipeToast.show(toastManager, createRecipeDisplay(entry)));
        }
    }

    private static RecipeDisplay createRecipeDisplay(RegistryEntry<Item> entry) {
        return new RecipeDisplay() {
            @Override
            public SlotDisplay result() {
                return new SlotDisplay.StackSlotDisplay(entry.value().getDefaultStack());
            }

            @Override
            public SlotDisplay craftingStation() {
                return new SlotDisplay.StackSlotDisplay(Items.AIR.getDefaultStack());
            }

            @Override
            public Serializer<? extends RecipeDisplay> serializer() {
                return null;
            }
        };
    }
}
