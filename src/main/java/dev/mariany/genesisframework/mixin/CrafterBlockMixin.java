package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.age.ServerAgeManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
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
     * Prevent Crafter from crafting an item that requires an age.
     */
    @WrapOperation(
            method = "getPotentialResults",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeCache;get(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/crafting/CraftingInput;)Ljava/util/Optional;"
            )
    )
    private static Optional<RecipeHolder<CraftingRecipe>> wrapGetCraftingRecipe(
            RecipeCache recipeCache,
            ServerLevel level,
            CraftingInput input,
            Operation<Optional<RecipeHolder<CraftingRecipe>>> original
    ) {
        Optional<RecipeHolder<CraftingRecipe>> optionalRecipe = original.call(recipeCache, level, input);

        if (optionalRecipe.isPresent()) {
            RecipeHolder<CraftingRecipe> recipe = optionalRecipe.get();
            ItemStack stack = recipe.value().assemble(input);

            if (ServerAgeManager.getInstance().isAgeGuarded(stack.getItem())) {
                return Optional.empty();
            }
        }

        return original.call(recipeCache, level, input);
    }
}
