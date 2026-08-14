package dev.mariany.genesisframework.age;

import dev.mariany.genesisframework.item.trait.ItemTrait;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record PartialAgeItemRestrictions(
        @Nullable Map<AgeMetadata, List<Ingredient>> gatedByAge,
        @Nullable Map<AgeMetadata, List<Ingredient>> lockedByAge,
        @Nullable Map<AgeMetadata, List<ItemTrait>> traitsByAge
) {
    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<Map<AgeMetadata, List<Ingredient>>>>
            OPTIONAL_ITEMS_BY_AGE_STREAM_CODEC = ByteBufCodecs.optional(
            AgeItemRestrictions.INGREDIENTS_BY_AGE_STREAM_CODEC
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<Map<AgeMetadata, List<ItemTrait>>>>
            OPTIONAL_TRAITS_BY_AGE_STREAM_CODEC = ByteBufCodecs.optional(
            AgeItemRestrictions.TRAITS_BY_AGE_STREAM_CODEC
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PartialAgeItemRestrictions> STREAM_CODEC =
            StreamCodec.composite(
                    OPTIONAL_ITEMS_BY_AGE_STREAM_CODEC,
                    PartialAgeItemRestrictions::optionalGatedByAge,
                    OPTIONAL_ITEMS_BY_AGE_STREAM_CODEC,
                    PartialAgeItemRestrictions::optionalLockedByAge,
                    OPTIONAL_TRAITS_BY_AGE_STREAM_CODEC,
                    PartialAgeItemRestrictions::optionalTraitsByAge,
                    (
                            optionalGatedByAge,
                            optionalLockedByAge,
                            optionalTraitsByAge
                    ) -> new PartialAgeItemRestrictions(
                            optionalGatedByAge.orElse(null),
                            optionalLockedByAge.orElse(null),
                            optionalTraitsByAge.orElse(null)
                    )
            );

    public PartialAgeItemRestrictions {
        gatedByAge = AgeItemRestrictions.copyNullable(gatedByAge);
        lockedByAge = AgeItemRestrictions.copyNullable(lockedByAge);
        traitsByAge = AgeItemRestrictions.copyNullable(traitsByAge);
    }

    public AgeItemRestrictions toAgeItemRestrictions(AgeItemRestrictions ageItemRestrictions) {
        return new AgeItemRestrictions(
                this.gatedByAge == null
                        ? ageItemRestrictions.gatedByAge()
                        : this.gatedByAge,
                this.lockedByAge == null
                        ? ageItemRestrictions.lockedByAge()
                        : this.lockedByAge,
                this.traitsByAge == null
                        ? ageItemRestrictions.traitsByAge()
                        : this.traitsByAge
        );
    }

    private Optional<Map<AgeMetadata, List<Ingredient>>> optionalGatedByAge() {
        return Optional.ofNullable(this.gatedByAge);
    }

    private Optional<Map<AgeMetadata, List<Ingredient>>> optionalLockedByAge() {
        return Optional.ofNullable(this.lockedByAge);
    }

    private Optional<Map<AgeMetadata, List<ItemTrait>>> optionalTraitsByAge() {
        return Optional.ofNullable(this.traitsByAge);
    }
}
