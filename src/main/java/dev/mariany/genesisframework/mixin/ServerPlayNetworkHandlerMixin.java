package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.event.server.recipe.ServerRecipeEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayer player;

    /**
     * Queries {@link ServerRecipeEvents#SHOULD_PREVENT_PLACEMENT} before placing a recipe into a menu.
     */
    @WrapOperation(
            method = "handlePlaceRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/stats/ServerRecipeBook;contains(Lnet/minecraft/resources/ResourceKey;)Z"
            )
    )
    public boolean wrapContains(
            ServerRecipeBook recipeBook,
            ResourceKey<Recipe<?>> recipeKey,
            Operation<Boolean> original
    ) {
        if (ServerRecipeEvents.SHOULD_PREVENT_PLACEMENT.invoker().shouldPreventPlacement(this.player, recipeKey)) {
            return false;
        }

        return original.call(recipeBook, recipeKey);
    }
}
