package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.event.client.recipe.ClientRecipeEvents;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(RecipeButton.class)
public class AnimatedResultButtonMixin {
    @Shadow
    private List<RecipeButton.ResolvedEntry> selectedEntries;

    /**
     * Passes recipe-button entries through {@link ClientRecipeEvents#MODIFY_BUTTON_ENTRIES}.
     */
    @WrapOperation(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeButton;allRecipesHaveSameResultDisplay(Ljava/util/List;)Z"
            )
    )
    public boolean injectAllRecipesHaveSameResultDisplay(List<?> results, Operation<Boolean> original) {
        this.selectedEntries = ClientRecipeEvents.MODIFY_BUTTON_ENTRIES
                .invoker()
                .modifyButtonEntries(this.selectedEntries);

        return original.call(this.selectedEntries);
    }
}
