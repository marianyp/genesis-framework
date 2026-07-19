package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.client.age.ClientAgeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplay;

@Mixin(RecipeDisplayEntry.class)
public class ClientRecipeDisplayEntryMixin {
    /**
     * Filter out any locked items from a recipe display.
     */
    @WrapOperation(
            method = "resultItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/display/SlotDisplay;resolveForStacks(Lnet/minecraft/util/context/ContextMap;)Ljava/util/List;"
            )
    )
    public List<ItemStack> wrapGetStacks(
            SlotDisplay slotDisplay,
            ContextMap parameters,
            Operation<List<ItemStack>> original
    ) {
        ClientAgeManager clientAgeManager = ClientAgeManager.getInstance();
        return original.call(slotDisplay, parameters).stream().filter(clientAgeManager::isUnlocked).toList();
    }
}
