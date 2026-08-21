package dev.mariany.genesisframework.event.item;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ItemStackEvents {
    public static final Event<StackDamaged> STACK_DAMAGED = EventFactory.createArrayBacked(
            StackDamaged.class,
            callbacks -> (stack, player) -> {
                for (StackDamaged callback : callbacks) {
                    callback.onStackDamaged(stack, player);
                }
            }
    );

    private ItemStackEvents() {
    }

    @FunctionalInterface
    public interface StackDamaged {
        void onStackDamaged(ItemStack stack, ServerPlayer player);
    }
}