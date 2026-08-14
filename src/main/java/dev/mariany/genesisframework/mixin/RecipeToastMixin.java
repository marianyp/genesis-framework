package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.event.client.recipe.ClientRecipeEvents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipeToast.class)
public class RecipeToastMixin {
    @Shadow
    @Final
    private List<RecipeToast.Entry> recipeItems;

    /**
     * Queries {@link ClientRecipeEvents#ALLOW_TOAST} before adding a recipe toast.
     */
    @WrapOperation(
            method = "addOrUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/toasts/RecipeToast;addItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private static void wrapAddItem(
            RecipeToast recipeToast,
            ItemStack categoryItem,
            ItemStack unlockedItem,
            Operation<Void> original
    ) {
        boolean allowToast = ClientRecipeEvents.ALLOW_TOAST.invoker().test(unlockedItem);

        if (!allowToast) {
            return;
        }

        original.call(recipeToast, categoryItem, unlockedItem);
    }

    /**
     * Queries {@link ClientRecipeEvents#ALLOW_TOAST_RENDER} before extracting recipe-toast render state.
     */
    @Inject(method = "extractRenderState", at = @At(value = "HEAD"), cancellable = true)
    public void injectExtractRenderState(
            GuiGraphicsExtractor context,
            Font textRenderer,
            long startTime,
            CallbackInfo ci
    ) {
        if (ClientRecipeEvents.ALLOW_TOAST_RENDER.invoker().allowToastRender(this.recipeItems)) {
            return;
        }

        ci.cancel();
    }
}
