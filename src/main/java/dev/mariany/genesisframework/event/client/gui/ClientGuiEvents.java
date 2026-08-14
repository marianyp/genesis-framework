package dev.mariany.genesisframework.event.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@Environment(EnvType.CLIENT)
public final class ClientGuiEvents {
    private ClientGuiEvents() {
    }

    public static final Event<BeforeToastsUpdate> BEFORE_TOASTS_UPDATE = EventFactory.createArrayBacked(
            BeforeToastsUpdate.class,
            callbacks -> () -> {
                for (BeforeToastsUpdate callback : callbacks) {
                    callback.beforeToastsUpdate();
                }
            }
    );

    public interface BeforeToastsUpdate {
        void beforeToastsUpdate();
    }
}
