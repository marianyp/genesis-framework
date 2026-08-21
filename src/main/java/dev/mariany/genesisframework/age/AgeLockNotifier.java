package dev.mariany.genesisframework.age;

import dev.mariany.genesisframework.packet.clientbound.NotifyAgeLockedPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ItemLike;

public final class AgeLockNotifier {
    private AgeLockNotifier() {
    }

    public static void notifyAgeLocked(String restrictedTranslation, AgeEntry ageEntry, ServerPlayer serverPlayer) {
        notifyAgeLocked(restrictedTranslation, ageEntry, false, serverPlayer);
    }

    public static void notifyAgeLockedClick(ItemLike item, AgeEntry ageEntry, ServerPlayer serverPlayer) {
        notifyAgeLockedClick(item.asItem().getDescriptionId(), ageEntry, serverPlayer);
    }

    public static void notifyAgeLockedClick(
            String restrictedTranslation,
            AgeEntry ageEntry,
            ServerPlayer serverPlayer
    ) {
        notifyAgeLocked(restrictedTranslation, ageEntry, true, serverPlayer);
    }

    private static void notifyAgeLocked(
            String restrictedTranslation,
            AgeEntry ageEntry,
            boolean clickInteraction,
            ServerPlayer serverPlayer
    ) {
        ServerPlayNetworking.send(
                serverPlayer,
                new NotifyAgeLockedPayload(restrictedTranslation, ageEntry.getId(), clickInteraction)
        );
    }
}
