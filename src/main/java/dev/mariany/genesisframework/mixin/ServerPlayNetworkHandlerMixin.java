package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.age.ServerAgeManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayer player;

    /**
     * Check if an item is unlocked when calling {@link ServerRecipeBook#contains(ResourceKey)}.
     */
    @WrapOperation(
            method = "handlePlaceRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/stats/ServerRecipeBook;contains(Lnet/minecraft/resources/ResourceKey;)Z"
            )
    )
    public boolean wrapOnCraftRequest(
            ServerRecipeBook recipeBook,
            ResourceKey<Recipe<?>> recipeKey,
            Operation<Boolean> original
    ) {
        MinecraftServer server = this.player.level().getServer();
        RecipeManager recipeManager = server.getRecipeManager();
        Optional<RecipeHolder<?>> recipeEntry = recipeManager.byKey(recipeKey);

        if (recipeEntry.isPresent()) {
            if (recipeEntry.get().value() instanceof CraftingRecipe craftingRecipe) {
                ItemStack stack = craftingRecipe.assemble(CraftingInput.EMPTY);

                if (!ServerAgeManager.getInstance().isUnlocked(this.player, stack)) {
                    return false;
                }
            }
        }

        return original.call(recipeBook, recipeKey);
    }
}
