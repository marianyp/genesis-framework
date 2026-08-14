package dev.mariany.genesisframework.age.restriction;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.event.server.recipe.ServerRecipeEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.Optional;

public final class AgeRecipeRestrictions {
    private AgeRecipeRestrictions() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Age Recipe Restrictions");
        ServerRecipeEvents.ALLOW_CRAFT.register(AgeRecipeRestrictions::canCraft);
        ServerRecipeEvents.SHOULD_PREVENT_PLACEMENT.register(AgeRecipeRestrictions::shouldPreventPlacement);
        ServerRecipeEvents.ALLOW_SMITHING.register(AgeRecipeRestrictions::canCraft);
        ServerRecipeEvents.SHOULD_PREVENT_CRAFTER_CRAFTING.register(
                AgeRecipeRestrictions::shouldPreventCrafterCrafting
        );
    }

    private static boolean canCraft(ServerPlayer player, RecipeHolder<?> recipe) {
        if (!(recipe.value() instanceof CraftingRecipe craftingRecipe)) {
            return true;
        }

        final ItemStack result;

        try {
            result = craftingRecipe.assemble(CraftingInput.EMPTY);
        } catch (Exception exception) {
            return true;
        }

        return result.isEmpty() || player.isCreative() || GenesisFramework.getServerAgeManager().isUnlocked(
                player,
                result
        );
    }

    private static boolean shouldPreventPlacement(ServerPlayer player, ResourceKey<Recipe<?>> recipeKey) {
        MinecraftServer server = player.level().getServer();
        Optional<RecipeHolder<?>> recipe = server.getRecipeManager().byKey(recipeKey);

        if (recipe.isEmpty() || !(recipe.get().value() instanceof CraftingRecipe craftingRecipe)) {
            return false;
        }

        ItemStack result = craftingRecipe.assemble(CraftingInput.EMPTY);

        return !GenesisFramework.getServerAgeManager().isUnlocked(player, result);
    }

    private static boolean canCraft(ServerPlayer player, SmithingRecipeInput input) {
        ServerLevel level = player.level();

        Optional<RecipeHolder<SmithingRecipe>> optionalRecipe = level
                .recipeAccess()
                .getRecipeFor(RecipeType.SMITHING, input, level);

        if (optionalRecipe.isEmpty()) {
            return true;
        }

        ItemStack result = optionalRecipe.get().value().assemble(input);

        if (isStackInSmithingInput(input, result)) {
            return true;
        }

        return GenesisFramework.getServerAgeManager().isUnlocked(player, result);
    }

    private static boolean isStackInSmithingInput(SmithingRecipeInput input, ItemStack stack) {
        for (int index = 0; index < input.size(); index++) {
            if (input.getItem(index).is(stack.getItem())) {
                return true;
            }
        }

        return false;
    }

    private static boolean shouldPreventCrafterCrafting(
            CraftingInput input,
            RecipeHolder<CraftingRecipe> recipeHolder
    ) {
        ItemStack result = recipeHolder.value().assemble(input);
        return GenesisFramework.getServerAgeManager().isAgeGuarded(result.getItem());
    }
}
