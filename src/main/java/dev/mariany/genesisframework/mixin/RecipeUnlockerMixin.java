package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.event.server.recipe.ServerRecipeEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeCraftingHolder.class)
public interface RecipeUnlockerMixin {
    /**
     * Queries {@link ServerRecipeEvents#ALLOW_CRAFT} before marking a recipe as used.
     */
    @Inject(
            method = "setRecipeUsed(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/RecipeHolder;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectSetRecipeUsed(ServerPlayer player, RecipeHolder<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        if (ServerRecipeEvents.ALLOW_CRAFT.invoker().allowCraft(player, recipe)) {
            return;
        }

        cir.setReturnValue(false);
    }
}

