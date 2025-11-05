package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.age.AgeManager;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeUnlocker.class)
public interface RecipeUnlockerMixin {
    /**
     * Check if an item is unlocked when calling {@link RecipeUnlocker#shouldCraftRecipe(ServerPlayerEntity, RecipeEntry)}.
     */
    @Inject(
            method = "shouldCraftRecipe",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectShouldCraftRecipe(
            ServerPlayerEntity player,
            RecipeEntry<?> recipe,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ServerWorld serverWorld = player.getEntityWorld();
        RegistryWrapper.WrapperLookup registries = serverWorld.getRegistryManager();
        AgeManager ageManager = AgeManager.getInstance();

        if (recipe.value() instanceof CraftingRecipe craftingRecipe) {
            ItemStack stack = craftingRecipe.craft(CraftingRecipeInput.EMPTY, registries);

            if (!ageManager.isUnlocked(player, stack) && !player.isCreative()) {
                cir.setReturnValue(false);
            }
        }
    }
}

