package dev.mariany.genesisframework.event.client.advancement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;

import java.util.Map;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public final class ClientAdvancementEvents {
    private ClientAdvancementEvents() {
    }

    public static final Event<Updated> UPDATED = EventFactory.createArrayBacked(
            Updated.class,
            callbacks -> (advancements, packet) -> {
                for (Updated callback : callbacks) {
                    callback.onUpdated(advancements, packet);
                }
            }
    );

    public static final Event<ScreenInitialized> SCREEN_INITIALIZED = EventFactory.createArrayBacked(
            ScreenInitialized.class,
            callbacks -> (tabs, advancements, selectionListener) -> {
                for (ScreenInitialized callback : callbacks) {
                    callback.onScreenInitialized(tabs, advancements, selectionListener);
                }
            }
    );

    public static final Event<AllowRoot> ALLOW_ROOT = EventFactory.createArrayBacked(
            AllowRoot.class,
            callbacks -> root -> {
                for (AllowRoot callback : callbacks) {
                    if (!callback.allowRoot(root)) {
                        return false;
                    }
                }

                return true;
            }
    );

    public interface Updated {
        void onUpdated(ClientAdvancements advancements, ClientboundUpdateAdvancementsPacket packet);
    }

    public interface ScreenInitialized {
        void onScreenInitialized(
                Map<AdvancementHolder, AdvancementTab> tabs,
                ClientAdvancements advancements,
                Consumer<AdvancementHolder> selectionListener
        );
    }

    public interface AllowRoot {
        boolean allowRoot(AdvancementNode root);
    }
}
