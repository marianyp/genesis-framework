package dev.mariany.genesisframework.event.server.level;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;
import java.util.UUID;

public final class TrialSpawnerEvents {
    private TrialSpawnerEvents() {
    }

    public static final Event<Completed> COMPLETED = EventFactory.createArrayBacked(
            Completed.class,
            callbacks -> (level, players, ominous) -> {
                for (Completed callback : callbacks) {
                    callback.onCompleted(level, players, ominous);
                }
            }
    );

    public interface Completed {
        void onCompleted(ServerLevel level, Set<UUID> players, boolean ominous);
    }
}
