package dev.mariany.genesisframework.advancement.criterion;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.event.item.ItemStackEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ItemBrokenTriggerHandler {
    private ItemBrokenTriggerHandler() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Item Broken Trigger Handler");
        ItemStackEvents.STACK_DAMAGED.register(ItemBrokenTriggerHandler::onStackDamaged);
    }

    private static void onStackDamaged(ItemStack stack, @Nullable ServerPlayer player) {
        if (!stack.isBroken()) {
            return;
        }

        GFCriteriaTriggers.ITEM_BROKEN.trigger(player, stack);
    }
}