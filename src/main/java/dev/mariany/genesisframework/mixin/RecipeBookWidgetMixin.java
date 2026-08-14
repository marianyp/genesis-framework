package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.event.client.recipe.ClientRecipeEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(RecipeBookComponent.class)
public class RecipeBookWidgetMixin {
    @Shadow
    protected Minecraft minecraft;

    /**
     * Passes recipe-book collections through {@link ClientRecipeEvents#MODIFY_COLLECTIONS}.
     */
    @WrapOperation(
            method = "updateCollections",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;updateCollections(Ljava/util/List;ZZ)V"
            )
    )
    private void wrapUpdateCollections(
            RecipeBookPage recipeBookPage,
            List<RecipeCollection> recipeCollections,
            boolean resetPage,
            boolean isFiltering,
            Operation<Void> original
    ) {
        List<RecipeCollection> modifiedCollections = ClientRecipeEvents.MODIFY_COLLECTIONS
                .invoker()
                .modifyCollections(recipeCollections, this.minecraft.level);

        original.call(
                recipeBookPage,
                modifiedCollections,
                resetPage,
                isFiltering
        );
    }
}
