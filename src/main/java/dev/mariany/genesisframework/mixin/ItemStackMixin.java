package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.event.item.ItemStackEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(
            method = "applyDamage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isBroken()Z")
    )
    private void injectOnDurabilityChange(
            int damage,
            @Nullable ServerPlayer player,
            Consumer<Item> breakCallback,
            CallbackInfo ci
    ) {
        if (player == null) {
            return;
        }

        ItemStack stack = ((ItemStack) (Object) this);

        ItemStackEvents.STACK_DAMAGED.invoker().onStackDamaged(stack, player);
    }
}