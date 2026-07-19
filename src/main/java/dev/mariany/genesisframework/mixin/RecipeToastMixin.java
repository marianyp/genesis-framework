package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.client.age.ClientAgeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.world.item.ItemStack;

@Mixin(RecipeToast.class)
public class RecipeToastMixin {
    @Shadow
    @Final
    private List<?> recipeItems;

    /**
     * Prevent showing recipes for items that are locked.
     */
    @WrapOperation(
            method = "addOrUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/toasts/RecipeToast;addItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private static void wrapShow(
            RecipeToast recipeToast,
            ItemStack categoryItem,
            ItemStack unlockedItem,
            Operation<Void> original
    ) {
        if (ClientAgeManager.getInstance().isUnlocked(unlockedItem)) {
            original.call(recipeToast, categoryItem, unlockedItem);
        }
    }

    /**
     * Prevent drawing toast when there aren't any display items (i.e. all items locked)
     */
    @Inject(method = "extractRenderState", at = @At(value = "HEAD"), cancellable = true)
    public void injectDraw(GuiGraphicsExtractor context, Font textRenderer, long startTime, CallbackInfo ci) {
        if (recipeItems.isEmpty()) {
            ci.cancel();
        }
    }
}
