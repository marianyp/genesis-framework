package dev.mariany.genesisframework.event.server.advancement;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import java.util.Map;

public final class ServerAdvancementEvents {
    private ServerAdvancementEvents() {
    }

    /**
     * Called when advancements are loaded or updated.
     */
    public static final Event<BeforeAdvancementsLoad> BEFORE_ADVANCEMENTS_LOAD = EventFactory.createArrayBacked(
            BeforeAdvancementsLoad.class,
            callbacks -> map -> {
                for (BeforeAdvancementsLoad callback : callbacks) {
                    callback.onAdvancementsLoaded(map);
                }
            });

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

    public interface BeforeAdvancementsLoad {
        /**
         * @param advancementMap Mutable map for advancements that will be loaded.
         */
        void onAdvancementsLoaded(Map<Identifier, Advancement> advancementMap);
    }

    public interface ShouldForceDisplay {
        boolean shouldForceDisplay(AdvancementNode advancement);
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
