package dev.mariany.genesisframework.event.client.advancement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public final class ClientAdvancementEvents {
    private ClientAdvancementEvents() {
    }

    public static final Event<Updated> UPDATED = EventFactory.createArrayBacked(
            Updated.class,
            callbacks -> (shouldReset, progress) -> {
                for (Updated callback : callbacks) {
                    callback.onUpdated(shouldReset, progress);
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
        void onUpdated(boolean shouldReset, Map<Identifier, AdvancementProgress> progress);
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
