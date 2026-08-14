package dev.mariany.genesisframework.event.server.age;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

public final class ServerAgeEvents {
    public static final Event<Sync> SYNC = EventFactory.createArrayBacked(
            Sync.class,
            callbacks -> serverPlayer -> {
                for (Sync callback : callbacks) {
                    callback.onSync(serverPlayer);
                }
            }
    );

    private ServerAgeEvents() {
    }

    public interface Sync {
        void onSync(ServerPlayer serverPlayer);
    }
}
