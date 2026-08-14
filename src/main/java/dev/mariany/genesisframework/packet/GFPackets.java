package dev.mariany.genesisframework.packet;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.packet.clientbound.NotifyAgeLockedPayload;
import dev.mariany.genesisframework.packet.clientbound.AgeItemRestrictionsPayload;
import dev.mariany.genesisframework.packet.clientbound.PartialAgeItemRestrictionsPayload;
import dev.mariany.genesisframework.packet.clientbound.UpdateInstructionsPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;

public final class GFPackets {
    private GFPackets() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Packets");
        clientBound(PayloadTypeRegistry.clientboundPlay());
        serverBound(PayloadTypeRegistry.serverboundPlay());
    }

    private static void clientBound(PayloadTypeRegistry<RegistryFriendlyByteBuf> registry) {
        registry.register(AgeItemRestrictionsPayload.ID, AgeItemRestrictionsPayload.STREAM_CODEC);
        registry.register(PartialAgeItemRestrictionsPayload.ID, PartialAgeItemRestrictionsPayload.STREAM_CODEC);
        registry.register(UpdateInstructionsPayload.ID, UpdateInstructionsPayload.STREAM_CODEC);
        registry.register(NotifyAgeLockedPayload.ID, NotifyAgeLockedPayload.STREAM_CODEC);
    }

    private static void serverBound(PayloadTypeRegistry<RegistryFriendlyByteBuf> registry) {
    }
}
