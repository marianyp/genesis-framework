package dev.mariany.genesisframework.packet.clientbound;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.age.AgeItemRestrictions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AgeItemRestrictionsPayload(AgeItemRestrictions ageItemRestrictions) implements CustomPacketPayload {
    public static final Type<AgeItemRestrictionsPayload> ID = new Type<>(
            GenesisFramework.id("age_item_restrictions")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AgeItemRestrictionsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    AgeItemRestrictions.STREAM_CODEC,
                    AgeItemRestrictionsPayload::ageItemRestrictions,
                    AgeItemRestrictionsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
