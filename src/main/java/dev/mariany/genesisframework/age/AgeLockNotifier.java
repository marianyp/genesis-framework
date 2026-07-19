package dev.mariany.genesisframework.age;

import dev.mariany.genesisframework.packet.clientbound.NotifyAgeLockedPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class AgeLockNotifier {
    public static void notifyAgeLocked(String locked, Age age, ServerPlayer serverPlayer) {
        notifyAgeLocked(locked, age, false, serverPlayer);
    }

    public static void notifyAgeLockedClick(String locked, Age age, ServerPlayer serverPlayer) {
        notifyAgeLocked(locked, age, true, serverPlayer);
    }

    private static void notifyAgeLocked(String locked, Age age, boolean clickInteraction, ServerPlayer serverPlayer) {
        ServerPlayNetworking.send(serverPlayer, new NotifyAgeLockedPayload(locked, age.display().title().getString(), clickInteraction));
    }
}
