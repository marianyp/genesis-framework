package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.age.AgeManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(SmithingScreenHandler.class)
public abstract class SmithingScreenHandlerMixin extends ForgingScreenHandler {
    public SmithingScreenHandlerMixin(
            @Nullable ScreenHandlerType<?> type,
            int syncId,
            PlayerInventory playerInventory,
            ScreenHandlerContext context,
            ForgingSlotsManager forgingSlotsManager
    ) {
        super(type, syncId, playerInventory, context, forgingSlotsManager);
    }

    @Shadow
    protected abstract SmithingRecipeInput createRecipeInput();

    /**
     * Prevent crafting an item in a Smithing Table.
     */
    @Inject(method = "updateResult", at = @At(value = "HEAD"), cancellable = true)
    public void injectUpdateResult(CallbackInfo ci) {
        SmithingRecipeInput smithingRecipeInput = this.createRecipeInput();

        if (this.player instanceof ServerPlayerEntity serverPlayer) {
            ServerWorld serverWorld = serverPlayer.getEntityWorld();

            Optional<RecipeEntry<SmithingRecipe>> optionalRecipeEntry = serverWorld.getRecipeManager().getFirstMatch(
                    RecipeType.SMITHING,
                    smithingRecipeInput,
                    serverWorld
            );

            optionalRecipeEntry.ifPresent(recipeEntry -> {
                        SmithingRecipe recipe = recipeEntry.value();
                        ItemStack itemStack = recipe.craft(
                                smithingRecipeInput,
                                serverWorld.getRegistryManager()
                        );

                        // Allows procedures like applying a smithing trim.
                        for (int i = 0; i < smithingRecipeInput.size(); i++) {
                            if (smithingRecipeInput.getStackInSlot(i).isOf(itemStack.getItem())) {
                                return;
                            }
                        }

                        if (!AgeManager.getInstance().isUnlocked(serverPlayer, itemStack)) {
                            ci.cancel();
                        }
                    }
            );
        }
    }
}
