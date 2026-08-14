package dev.mariany.genesisframework.age;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public record AgeMetadata(Identifier id, Component title, @Nullable Component category) {
    public static final StreamCodec<RegistryFriendlyByteBuf, AgeMetadata> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            AgeMetadata::id,
            ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC,
            AgeMetadata::title,
            ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC,
            ageMetadata -> Optional.ofNullable(ageMetadata.category),
            (
                    id,
                    title,
                    optionalCategory
            ) -> new AgeMetadata(id, title, optionalCategory.orElse(null))
    );

    public AgeMetadata {
        title = title.copy();
        category = category == null ? null : category.copy();
    }

    public static AgeMetadata create(Identifier id, Age age) {
        Optional<Component> optionalCategory = AgeEntry
                .getCategory(id)
                .map(value -> Component.translatable("age." + id.getNamespace() + ".category." + value));

        return new AgeMetadata(id, age.display().title(), optionalCategory.orElse(null));
    }

    public Component component() {
        return component(this.title, this.category);
    }

    private static Component component(Component title, @Nullable Component category) {
        Component age = Component.translatable("age.genesisframework.age");

        if (category == null) {
            return Component.translatable("age.genesisframework.name", title, age);
        }

        return Component.translatable("age.genesisframework.name.categorized", category, title, age);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof AgeMetadata other && this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
