package dev.mariany.genesisframework.event.server.advancement;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;

public final class ServerAdvancementEvents {
    private ServerAdvancementEvents() {
    }

    public static final Event<CreateManager> CREATE_MANAGER = EventFactory.createArrayBacked(
            CreateManager.class,
            callbacks -> (registries, original) -> {
                AdvancementManagerFactory factory = original;

                for (int index = callbacks.length - 1; index >= 0; index--) {
                    CreateManager callback = callbacks[index];
                    AdvancementManagerFactory next = factory;
                    factory = provider -> callback.createManager(provider, next);
                }

                return factory.create(registries);
            }
    );

    public static final Event<AllowTreeBuild> ALLOW_TREE_BUILD = EventFactory.createArrayBacked(
            AllowTreeBuild.class,
            callbacks -> manager -> {
                for (AllowTreeBuild callback : callbacks) {
                    if (!callback.allowTreeBuild(manager)) {
                        return false;
                    }
                }

                return true;
            }
    );

    public static final Event<FinalizeReloadManager> FINALIZE_RELOAD_MANAGER = EventFactory.createArrayBacked(
            FinalizeReloadManager.class,
            callbacks -> (manager, registries) -> {
                for (FinalizeReloadManager callback : callbacks) {
                    callback.onFinalizeReloadManager(manager, registries);
                }
            }
    );

    public static final Event<ShouldForceDisplay> SHOULD_FORCE_DISPLAY = EventFactory.createArrayBacked(
            ShouldForceDisplay.class,
            callbacks -> advancement -> {
                for (ShouldForceDisplay callback : callbacks) {
                    if (callback.shouldForceDisplay(advancement)) {
                        return true;
                    }
                }

                return false;
            }
    );

    public static final Event<AllowAward> ALLOW_AWARD = EventFactory.createArrayBacked(
            AllowAward.class,
            callbacks -> (player, advancement) -> {
                for (AllowAward callback : callbacks) {
                    if (!callback.allowAward(player, advancement)) {
                        return false;
                    }
                }

                return true;
            }
    );

    /**
     * Called when an advancement's granted or revoked state is updated for a player.
     */
    public static final Event<CompletionUpdated> COMPLETION_UPDATED = EventFactory.createArrayBacked(
            CompletionUpdated.class,
            callbacks -> (player, advancement) -> {
                for (CompletionUpdated callback : callbacks) {
                    callback.onCompletionUpdated(player, advancement);
                }
            }
    );

    public static final Event<Awarded> AWARDED = EventFactory.createArrayBacked(
            Awarded.class,
            callbacks -> (player, advancement) -> {
                for (Awarded callback : callbacks) {
                    callback.onAwarded(player, advancement);
                }
            }
    );

    public static final Event<TabAction> TAB_ACTION = EventFactory.createArrayBacked(
            TabAction.class,
            callbacks -> (player, packet) -> {
                for (TabAction callback : callbacks) {
                    callback.onTabAction(player, packet);
                }
            }
    );

    public interface ShouldForceDisplay {
        boolean shouldForceDisplay(AdvancementNode advancement);
    }

    public interface AdvancementManagerFactory {
        ServerAdvancementManager create(HolderLookup.Provider registries);
    }

    public interface CreateManager {
        ServerAdvancementManager createManager(
                HolderLookup.Provider registries,
                AdvancementManagerFactory original
        );
    }

    public interface AllowTreeBuild {
        boolean allowTreeBuild(ServerAdvancementManager manager);
    }

    public interface FinalizeReloadManager {
        void onFinalizeReloadManager(ServerAdvancementManager manager, HolderLookup.Provider registries);
    }

    public interface AllowAward {
        boolean allowAward(ServerPlayer player, AdvancementHolder advancement);
    }

    public interface CompletionUpdated {
        void onCompletionUpdated(ServerPlayer player, AdvancementHolder advancement);
    }

    public interface Awarded {
        void onAwarded(ServerPlayer player, AdvancementHolder advancement);
    }

    public interface TabAction {
        void onTabAction(ServerPlayer player, ServerboundSeenAdvancementsPacket packet);
    }
}
