package dev.mariany.genesisframework.event.client.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public final class ClientItemEvents {
    private ClientItemEvents() {
    }

    public static final Event<AddAttributeTooltips> ADD_ATTRIBUTE_TOOLTIPS = EventFactory.createArrayBacked(
            AddAttributeTooltips.class,
            callbacks -> (stack, display, player, consumer) -> {
                for (AddAttributeTooltips callback : callbacks) {
                    callback.addAttributeTooltips(stack, display, player, consumer);
                }
            }
    );

    public interface AddAttributeTooltips {
        void addAttributeTooltips(
                ItemStack stack,
                TooltipDisplay display,
                @Nullable Player player,
                Consumer<Component> consumer
        );
    }
}
