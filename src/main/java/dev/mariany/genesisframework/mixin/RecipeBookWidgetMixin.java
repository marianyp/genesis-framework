package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.client.age.ClientAgeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

@Mixin(RecipeBookComponent.class)
public class RecipeBookWidgetMixin {
    @Shadow
    protected Minecraft minecraft;

    /**
     * Filter Recipe Book results if ANY of the recipe display stacks are locked, to prevent confusion.
     */
    @WrapOperation(
            method = "updateCollections",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;updateCollections(Ljava/util/List;ZZ)V"
            )
    )
    private void filterLockedRecipes(
            RecipeBookPage recipeBookResults,
            List<RecipeCollection> resultCollections,
            boolean resetCurrentPage,
            boolean filteringCraftable,
            Operation<Void> original
    ) {
        ClientAgeManager clientAgeManager = ClientAgeManager.getInstance();
        ContextMap contextParameterMap = SlotDisplayContext.fromLevel(
                Objects.requireNonNull(this.minecraft.level)
        );

        List<RecipeCollection> filteredRecipes = resultCollections.stream().filter(resultCollection -> {
            for (RecipeDisplayEntry recipe : resultCollection.getRecipes()) {
                List<ItemStack> stacks = recipe.resultItems(contextParameterMap);
                if (stacks.stream().filter(clientAgeManager::isUnlocked).findAny().isEmpty()) {
                    return false;
                }
            }
            return true;
        }).toList();

        original.call(recipeBookResults, filteredRecipes, resetCurrentPage, filteringCraftable);
    }
}
