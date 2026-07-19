package dev.mariany.genesisframework.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;

public record ItemTrait(Ingredient ingredient, ItemAttributeModifiers attributeModifiers) {
    public static final Codec<ItemTrait> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("items").forGetter(ItemTrait::ingredient),
            ItemAttributeModifiers.CODEC.fieldOf("attribute_modifiers").forGetter(ItemTrait::attributeModifiers)
    ).apply(instance, ItemTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemTrait> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            ItemTrait::ingredient,
            ItemAttributeModifiers.STREAM_CODEC,
            ItemTrait::attributeModifiers,
            ItemTrait::new
    );
}
