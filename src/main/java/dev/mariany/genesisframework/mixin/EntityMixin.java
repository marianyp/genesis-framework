package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.age.AgeLockNotifier;
import dev.mariany.genesisframework.age.ServerAgeManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;


@Mixin(Entity.class)
public class EntityMixin {
    /**
     * Prevent teleporting via a portal if the target dimension is not unlocked.
     */
    @WrapOperation(
            method = "handlePortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/PortalProcessor;getPortalDestination(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/level/portal/TeleportTransition;"
            )
    )
    protected TeleportTransition wrapCreateTeleportTarget(
            PortalProcessor portalManager,
            ServerLevel level,
            Entity entity,
            Operation<TeleportTransition> original
    ) {
        TeleportTransition target = original.call(portalManager, level, entity);

        if (target != null) {
            boolean notify = entity instanceof ServerPlayer;
            Optional<ServerPlayer> optionalPlayer = getPlayerForPortalCheck(entity);

            if (optionalPlayer.isPresent()) {
                ServerAgeManager serverAgeManager = ServerAgeManager.getInstance();
                ServerPlayer player = optionalPlayer.get();
                ResourceKey<Level> worldRegistryKey = target.newLevel().dimension();

                if (!serverAgeManager.isUnlocked(player, worldRegistryKey)) {
                    Optional<AgeEntry> optionalAgeEntry = serverAgeManager.getRequiredAges(worldRegistryKey)
                                                                          .stream()
                                                                          .findAny();

                    if (notify) {
                        optionalAgeEntry.ifPresent(ageEntry ->
                                AgeLockNotifier.notifyAgeLocked(
                                        "tutorial.genesisframework.ageLocked.dimension",
                                        ageEntry.getAge(),
                                        player
                                )
                        );
                    }

                    return null;
                }
            }
        }

        return target;
    }

    @Unique
    private Optional<ServerPlayer> getPlayerForPortalCheck(Entity entity) {
        ServerPlayer player = null;

        if (entity instanceof ServerPlayer serverPlayer) {
            player = serverPlayer;
        } else if (entity instanceof TraceableEntity ownable && ownable.getOwner() instanceof ServerPlayer serverPlayer) {
            player = serverPlayer;
        } else if (
                entity instanceof OwnableEntity tameable && tameable.getOwner() instanceof ServerPlayer serverPlayer
        ) {
            player = serverPlayer;
        }

        return Optional.ofNullable(player);
    }
}
