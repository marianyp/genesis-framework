package dev.mariany.genesisframework.packet;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.packet.clientbound.NotifyAgeLockedPayload;
import dev.mariany.genesisframework.packet.clientbound.UpdateItemTraitsPayload;
import dev.mariany.genesisframework.packet.clientbound.UpdateLockedItemsPayload;
import dev.mariany.genesisframework.packet.clientbound.UpdateInstructionsPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class GFPackets {
    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Packets");
        clientBound(PayloadTypeRegistry.clientboundPlay());
        serverBound(PayloadTypeRegistry.serverboundPlay());
    }

    private static void clientBound(PayloadTypeRegistry<RegistryFriendlyByteBuf> registry) {
        registry.register(UpdateLockedItemsPayload.ID, UpdateLockedItemsPayload.STREAM_CODEC);
        registry.register(UpdateItemTraitsPayload.ID, UpdateItemTraitsPayload.STREAM_CODEC);
        registry.register(UpdateInstructionsPayload.ID, UpdateInstructionsPayload.STREAM_CODEC);
        registry.register(NotifyAgeLockedPayload.ID, NotifyAgeLockedPayload.STREAM_CODEC);
    }

    private static void serverBound(PayloadTypeRegistry<RegistryFriendlyByteBuf> registry) {
    }
}
