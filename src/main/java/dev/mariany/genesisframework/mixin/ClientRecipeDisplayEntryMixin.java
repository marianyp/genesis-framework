package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.event.client.recipe.ClientRecipeEvents;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(RecipeDisplayEntry.class)
public class ClientRecipeDisplayEntryMixin {
    /**
     * Passes recipe display items through {@link ClientRecipeEvents#MODIFY_DISPLAY_ITEMS}.
     */
    @WrapOperation(
            method = "resultItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/display/SlotDisplay;resolveForStacks(Lnet/minecraft/util/context/ContextMap;)Ljava/util/List;"
            )
    )
    public List<ItemStack> wrapResolveForStacks(
            SlotDisplay slotDisplay,
            ContextMap parameters,
            Operation<List<ItemStack>> original
    ) {
        List<ItemStack> items = original.call(slotDisplay, parameters);
        return ClientRecipeEvents.MODIFY_DISPLAY_ITEMS.invoker().modifyDisplayItems(items);
    }
}
