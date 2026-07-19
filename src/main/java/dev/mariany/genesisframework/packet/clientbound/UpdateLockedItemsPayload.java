package dev.mariany.genesisframework.packet.clientbound;

import dev.mariany.genesisframework.GenesisFramework;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public record UpdateLockedItemsPayload(List<Ingredient> items) implements CustomPacketPayload {
    public static final Type<UpdateLockedItemsPayload> ID = new Type<>(
            GenesisFramework.id("update_locked_items")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateLockedItemsPayload> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
            UpdateLockedItemsPayload::items,
            UpdateLockedItemsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
