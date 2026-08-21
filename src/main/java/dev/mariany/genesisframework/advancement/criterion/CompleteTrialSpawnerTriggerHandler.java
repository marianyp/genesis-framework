package dev.mariany.genesisframework.advancement.criterion;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.event.server.level.TrialSpawnerEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class CompleteTrialSpawnerTriggerHandler {
    private CompleteTrialSpawnerTriggerHandler() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Complete Trial Spawner Trigger Handler");
        TrialSpawnerEvents.COMPLETED.register(CompleteTrialSpawnerTriggerHandler::onTrialSpawnerCompleted);
    }

    private static void onTrialSpawnerCompleted(
            ServerLevel level,
            Set<UUID> players,
            boolean ominous
    ) {
        Optional<UUID> optionalPlayerId = players.stream().findFirst();

        if (optionalPlayerId.isEmpty()) {
            return;
        }

        Player player = level.getPlayerByUUID(optionalPlayerId.get());

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        GFCriteriaTriggers.COMPLETE_TRIAL_SPAWNER_ADVANCEMENT.trigger(serverPlayer, ominous);
    }
}
