package dev.mariany.genesisframework.compat.rei;

import dev.mariany.genesisframework.compat.AgeRecipeTransferMessages;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;

public final class REIAgeTransferHandler implements TransferHandler {
    @Override
    public double getPriority() {
        return Double.MAX_VALUE;
    }

    @Override
    public Result handle(Context context) {
        List<ItemStack> outputs = context
                .getDisplay()
                .getOutputEntries()
                .stream()
                .flatMap(Collection::stream)
                .filter(entry -> entry.getType().equals(VanillaEntryTypes.ITEM))
                .map(REIAgeTransferHandler::castStack)
                .toList();

        return AgeRecipeTransferMessages
                .forLockedOutputs(outputs)
                .map(message -> Result.createFailed(message).blocksFurtherHandling())
                .orElseGet(Result::createNotApplicable);
    }

    private static ItemStack castStack(EntryStack<?> entryStack) {
        return entryStack.castValue();
    }
}
