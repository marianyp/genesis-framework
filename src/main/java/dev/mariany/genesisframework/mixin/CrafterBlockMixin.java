package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.event.server.recipe.ServerRecipeEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeCache;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.CrafterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(CrafterBlock.class)
public class CrafterBlockMixin {
    /**
     * Queries {@link ServerRecipeEvents#SHOULD_PREVENT_CRAFTER_CRAFTING} before the Crafter crafts a recipe.
     */
    @WrapOperation(
            method = "getPotentialResults",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeCache;get(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/crafting/CraftingInput;)Ljava/util/Optional;"
            )
    )
    private static Optional<RecipeHolder<CraftingRecipe>> wrapGet(
            RecipeCache recipeCache,
            ServerLevel level,
            CraftingInput input,
            Operation<Optional<RecipeHolder<CraftingRecipe>>> original
    ) {
        RecipeHolder<CraftingRecipe> recipeHolder = original.call(recipeCache, level, input).orElse(null);

        if (recipeHolder == null) {
            return Optional.empty();
        }

        boolean preventCrafterCrafting = ServerRecipeEvents.SHOULD_PREVENT_CRAFTER_CRAFTING
                .invoker()
                .shouldPreventCrafterCrafting(input, recipeHolder);

        if (preventCrafterCrafting) {
            return Optional.empty();
        }

        return Optional.of(recipeHolder);
    }
}
