package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.age.ServerAgeManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;

@Mixin(SmithingMenu.class)
public abstract class SmithingScreenHandlerMixin extends ItemCombinerMenu {
    public SmithingScreenHandlerMixin(
            @Nullable MenuType<?> type,
            int syncId,
            Inventory playerInventory,
            ContainerLevelAccess context,
            ItemCombinerMenuSlotDefinition forgingSlotsManager
    ) {
        super(type, syncId, playerInventory, context, forgingSlotsManager);
    }

    @Shadow
    protected abstract SmithingRecipeInput createRecipeInput();

    /**
     * Prevent crafting an item in a Smithing Table.
     */
    @Inject(method = "createResult", at = @At(value = "HEAD"), cancellable = true)
    public void injectUpdateResult(CallbackInfo ci) {
        SmithingRecipeInput smithingRecipeInput = this.createRecipeInput();

        if (this.player instanceof ServerPlayer serverPlayer) {
            ServerLevel serverWorld = serverPlayer.level();

            Optional<RecipeHolder<SmithingRecipe>> optionalRecipeEntry = serverWorld.recipeAccess().getRecipeFor(
                    RecipeType.SMITHING,
                    smithingRecipeInput,
                    serverWorld
            );

            optionalRecipeEntry.ifPresent(recipeEntry -> {
                ItemStack itemStack = recipeEntry.value().assemble(smithingRecipeInput);

                // Allows procedures like applying a smithing trim.
                for (int i = 0; i < smithingRecipeInput.size(); i++) {
                    if (smithingRecipeInput.getItem(i).is(itemStack.getItem())) {
                        return;
                    }
                }

                if (!ServerAgeManager.getInstance().isUnlocked(serverPlayer, itemStack)) {
                    ci.cancel();
                }
            });
        }
    }
}
