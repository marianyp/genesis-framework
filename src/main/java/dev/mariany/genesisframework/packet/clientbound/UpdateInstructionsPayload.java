package dev.mariany.genesisframework.packet.clientbound;

import dev.mariany.genesisframework.GenesisFramework;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record UpdateInstructionsPayload(List<Identifier> instructions) implements CustomPacketPayload {
    public static final Type<UpdateInstructionsPayload> ID = new Type<>(
            GenesisFramework.id("update_instructions")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateInstructionsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    UpdateInstructionsPayload::instructions,
                    UpdateInstructionsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
