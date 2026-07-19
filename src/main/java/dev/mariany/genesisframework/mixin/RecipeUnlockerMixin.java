package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.age.ServerAgeManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeCraftingHolder.class)
public interface RecipeUnlockerMixin {
    /**
     * Check if an item is unlocked when calling {@link RecipeCraftingHolder#setRecipeUsed(ServerPlayer, RecipeHolder)}.
     */
    @Inject(
            method = "setRecipeUsed(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/RecipeHolder;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectShouldCraftRecipe(
            ServerPlayer player,
            RecipeHolder<?> recipe,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ServerAgeManager serverAgeManager = ServerAgeManager.getInstance();

        if (recipe.value() instanceof CraftingRecipe craftingRecipe) {
            final ItemStack stack;

            try {
                stack = craftingRecipe.assemble(CraftingInput.EMPTY);
            } catch (Exception exception) {
                return;
            }

            if (!stack.isEmpty() && !serverAgeManager.isUnlocked(player, stack) && !player.isCreative()) {
                cir.setReturnValue(false);
            }
        }
    }
}

