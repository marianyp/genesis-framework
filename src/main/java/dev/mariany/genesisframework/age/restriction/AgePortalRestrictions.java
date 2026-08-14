package dev.mariany.genesisframework.age.restriction;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.age.AgeLockNotifier;
import dev.mariany.genesisframework.age.ServerAgeManager;
import dev.mariany.genesisframework.event.server.entity.ServerEntityEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class AgePortalRestrictions {
    private AgePortalRestrictions() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Age Portal Restrictions");
        ServerEntityEvents.MODIFY_PORTAL_DESTINATION.register(AgePortalRestrictions::filterDestination);
    }

    private static @Nullable TeleportTransition filterDestination(
            Entity entity,
            @Nullable TeleportTransition destination
    ) {
        if (destination == null) {
            return null;
        }

        Optional<ServerPlayer> optionalServerPlayer = getServerPlayer(entity);

        if (optionalServerPlayer.isEmpty()) {
            return destination;
        }

        ServerAgeManager ageManager = GenesisFramework.getServerAgeManager();
        ServerPlayer serverPlayer = optionalServerPlayer.get();
        ResourceKey<Level> dimension = destination.newLevel().dimension();

        if (ageManager.isUnlocked(serverPlayer, dimension)) {
            return destination;
        }

        if (!(entity instanceof ServerPlayer)) {
            return null;
        }

        ageManager.getRequiredAges(dimension)
                  .stream()
                  .findAny()
                  .ifPresent(ageEntry -> AgeLockNotifier.notifyAgeLocked(
                          "tutorial.genesisframework.age_locked.dimension",
                          ageEntry,
                          serverPlayer
                  ));

        return null;
    }

    private static Optional<ServerPlayer> getServerPlayer(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            return Optional.of(player);
        }

        if (entity instanceof TraceableEntity traceable && traceable.getOwner() instanceof ServerPlayer player) {
            return Optional.of(player);
        }

        if (entity instanceof OwnableEntity ownable && ownable.getOwner() instanceof ServerPlayer player) {
            return Optional.of(player);
        }

        return Optional.empty();
    }
}
