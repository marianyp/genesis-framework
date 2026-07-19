package dev.mariany.genesisframework.packet.clientbound;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.item.ItemTrait;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record UpdateItemTraitsPayload(Map<String, List<ItemTrait>> traitsByLanguageKey) implements CustomPacketPayload {
    public static final Type<UpdateItemTraitsPayload> ID = new Type<>(
            GenesisFramework.id("update_item_traits")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, Map<String, List<ItemTrait>>> ITEM_TRAITS_MAP_CODEC =
            ByteBufCodecs.map(
                    HashMap::new,
                    ByteBufCodecs.STRING_UTF8,
                    ItemTrait.STREAM_CODEC.apply(ByteBufCodecs.list())
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateItemTraitsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ITEM_TRAITS_MAP_CODEC,
                    UpdateItemTraitsPayload::traitsByLanguageKey,
                    UpdateItemTraitsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
