package dev.mariany.genesisframework.packet.clientbound;

import dev.mariany.genesisframework.GenesisFramework;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record NotifyAgeLockedPayload(
        String itemTranslation,
        String ageTranslation,
        boolean clickInteraction
) implements CustomPacketPayload {
    public static final Type<NotifyAgeLockedPayload> ID = new Type<>(
            GenesisFramework.id("notify_age_locked")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, NotifyAgeLockedPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, NotifyAgeLockedPayload::itemTranslation,
                    ByteBufCodecs.STRING_UTF8, NotifyAgeLockedPayload::ageTranslation,
                    ByteBufCodecs.BOOL, NotifyAgeLockedPayload::clickInteraction,
                    NotifyAgeLockedPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
