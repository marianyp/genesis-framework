package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Method;
import java.util.List;

@Mixin(RecipeButton.class)
public class AnimatedResultButtonMixin {
    @Shadow
    private List<?> selectedEntries;

    /**
     * Remove any recipe entries that have empty display items.
     */
    @WrapOperation(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeButton;allRecipesHaveSameResultDisplay(Ljava/util/List;)Z"
            )
    )
    public boolean injectShowResultCollection(
            List<?> results, Operation<Boolean> original
    ) {
        this.selectedEntries = this.selectedEntries.stream()
                .filter(AnimatedResultButtonMixin::hasDisplayItems)
                .toList();

        return original.call(this.selectedEntries);
    }

    @Unique
    private static boolean hasDisplayItems(Object result) {
        try {
            Method displayItems = result.getClass().getDeclaredMethod("displayItems");
            displayItems.setAccessible(true);
            Object value = displayItems.invoke(result);
            return value instanceof List<?> list && !list.isEmpty();
        } catch (ReflectiveOperationException exception) {
            return true;
        }
    }
}
