package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.event.client.item.ClientItemEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ClientItemStackMixin {
    /**
     * Invokes {@link ClientItemEvents#ADD_ATTRIBUTE_TOOLTIPS} after vanilla attribute tooltips are added.
     */
    @Inject(method = "addAttributeTooltips", at = @At(value = "TAIL"))
    private void injectAddAttributeTooltips(
            Consumer<Component> consumer,
            TooltipDisplay display,
            @Nullable Player player,
            CallbackInfo ci
    ) {
        ItemStack stack = (ItemStack) (Object) this;
        ClientItemEvents.ADD_ATTRIBUTE_TOOLTIPS.invoker().addAttributeTooltips(stack, display, player, consumer);
    }
}
