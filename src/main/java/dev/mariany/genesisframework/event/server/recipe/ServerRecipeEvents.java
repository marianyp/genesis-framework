package dev.mariany.genesisframework.event.server.recipe;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipeInput;

public final class ServerRecipeEvents {
    private ServerRecipeEvents() {
    }

    public static final Event<AllowCraft> ALLOW_CRAFT = EventFactory.createArrayBacked(
            AllowCraft.class,
            callbacks -> (player, recipe) -> {
                for (AllowCraft callback : callbacks) {
                    if (!callback.allowCraft(player, recipe)) {
                        return false;
                    }
                }

                return true;
            }
    );

    public static final Event<ShouldPreventPlacement> SHOULD_PREVENT_PLACEMENT = EventFactory.createArrayBacked(
            ShouldPreventPlacement.class,
            callbacks -> (player, recipeKey) -> {
                for (ShouldPreventPlacement callback : callbacks) {
                    if (callback.shouldPreventPlacement(player, recipeKey)) {
                        return true;
                    }
                }

                return false;
            }
    );

    public static final Event<AllowSmithing> ALLOW_SMITHING = EventFactory.createArrayBacked(
            AllowSmithing.class,
            callbacks -> (player, input) -> {
                for (AllowSmithing callback : callbacks) {
                    if (!callback.allowSmithing(player, input)) {
                        return false;
                    }
                }

                return true;
            }
    );

    public static final Event<ShouldPreventCrafterCrafting> SHOULD_PREVENT_CRAFTER_CRAFTING = EventFactory.createArrayBacked(
            ShouldPreventCrafterCrafting.class,
            callbacks -> (input, recipe) -> {
                for (ShouldPreventCrafterCrafting callback : callbacks) {
                    if (callback.shouldPreventCrafterCrafting(input, recipe)) {
                        return true;
                    }
                }

                return false;
            }
    );

    public interface AllowCraft {
        boolean allowCraft(ServerPlayer player, RecipeHolder<?> recipe);
    }

    public interface ShouldPreventPlacement {
        boolean shouldPreventPlacement(ServerPlayer player, ResourceKey<Recipe<?>> recipeKey);
    }

    public interface AllowSmithing {
        boolean allowSmithing(ServerPlayer player, SmithingRecipeInput input);
    }

    public interface ShouldPreventCrafterCrafting {
        boolean shouldPreventCrafterCrafting(CraftingInput input, RecipeHolder<CraftingRecipe> recipe);
    }
}
