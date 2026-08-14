package dev.mariany.genesisframework.packet.clientbound;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.age.PartialAgeItemRestrictions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PartialAgeItemRestrictionsPayload(PartialAgeItemRestrictions ageItemRestrictions)
        implements CustomPacketPayload {
    public static final Type<PartialAgeItemRestrictionsPayload> ID = new Type<>(
            GenesisFramework.id("partial_age_item_restrictions")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PartialAgeItemRestrictionsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    PartialAgeItemRestrictions.STREAM_CODEC,
                    PartialAgeItemRestrictionsPayload::ageItemRestrictions,
                    PartialAgeItemRestrictionsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
