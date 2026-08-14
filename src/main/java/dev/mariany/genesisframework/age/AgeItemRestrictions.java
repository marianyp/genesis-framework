package dev.mariany.genesisframework.age;

import dev.mariany.genesisframework.item.trait.ItemTrait;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record AgeItemRestrictions(
        Map<AgeMetadata, List<Ingredient>> gatedByAge,
        Map<AgeMetadata, List<Ingredient>> lockedByAge,
        Map<AgeMetadata, List<ItemTrait>> traitsByAge
) {
    public static final AgeItemRestrictions EMPTY = new AgeItemRestrictions(Map.of(), Map.of(), Map.of());

    public static final StreamCodec<RegistryFriendlyByteBuf, Map<AgeMetadata, List<Ingredient>>>
            INGREDIENTS_BY_AGE_STREAM_CODEC = ByteBufCodecs.map(
            HashMap::new,
            AgeMetadata.STREAM_CODEC,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list())
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, Map<AgeMetadata, List<ItemTrait>>>
            TRAITS_BY_AGE_STREAM_CODEC = ByteBufCodecs.map(
            HashMap::new,
            AgeMetadata.STREAM_CODEC,
            ItemTrait.STREAM_CODEC.apply(ByteBufCodecs.list())
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AgeItemRestrictions> STREAM_CODEC =
            StreamCodec.composite(
                    INGREDIENTS_BY_AGE_STREAM_CODEC,
                    AgeItemRestrictions::gatedByAge,
                    INGREDIENTS_BY_AGE_STREAM_CODEC,
                    AgeItemRestrictions::lockedByAge,
                    TRAITS_BY_AGE_STREAM_CODEC,
                    AgeItemRestrictions::traitsByAge,
                    AgeItemRestrictions::new
            );

    public AgeItemRestrictions {
        gatedByAge = copy(gatedByAge);
        lockedByAge = copy(lockedByAge);
        traitsByAge = copy(traitsByAge);
    }

    static <T> Map<AgeMetadata, List<T>> copyNullable(Map<AgeMetadata, List<T>> valuesByAge) {
        return valuesByAge == null ? null : copy(valuesByAge);
    }

    private static <T> Map<AgeMetadata, List<T>> copy(Map<AgeMetadata, List<T>> valuesByAge) {
        return valuesByAge
                .entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, AgeItemRestrictions::copy));
    }

    private static <T> List<T> copy(Map.Entry<AgeMetadata, List<T>> entry) {
        return List.copyOf(entry.getValue());
    }
}
