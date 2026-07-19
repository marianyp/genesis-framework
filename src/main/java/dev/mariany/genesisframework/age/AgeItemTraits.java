package dev.mariany.genesisframework.age;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mariany.genesisframework.item.ItemTrait;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public record AgeItemTraits(List<ItemTrait> beforeDone, List<ItemTrait> afterDone) {
    public static final Codec<AgeItemTraits> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                                        ItemTrait.CODEC
                                                .listOf()
                                                .optionalFieldOf("before_done", List.of())
                                                .forGetter(AgeItemTraits::beforeDone),
                                        ItemTrait.CODEC
                                                .listOf()
                                                .optionalFieldOf("after_done", List.of())
                                                .forGetter(AgeItemTraits::afterDone)
                                )
                                .apply(instance, AgeItemTraits::new)
    );

    @SuppressWarnings("unused")
    public static class Builder {
        private final List<ItemTrait> beforeDone = new ArrayList<>();
        private final List<ItemTrait> afterDone = new ArrayList<>();

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder beforeDone(Ingredient ingredient, ItemAttributeModifiers attributeModifiers) {
            return this.beforeDone(new ItemTrait(ingredient, attributeModifiers));
        }

        public Builder beforeDone(ItemTrait itemTrait) {
            this.beforeDone.add(itemTrait);
            return this;
        }

        public Builder beforeDone(List<ItemTrait> itemTraits) {
            this.beforeDone.addAll(itemTraits);
            return this;
        }

        public Builder afterDone(Ingredient ingredient, ItemAttributeModifiers attributeModifiers) {
            return this.beforeDone(new ItemTrait(ingredient, attributeModifiers));
        }

        public Builder afterDone(ItemTrait itemTrait) {
            this.afterDone.add(itemTrait);
            return this;
        }

        public Builder afterDone(List<ItemTrait> itemTraits) {
            this.afterDone.addAll(itemTraits);
            return this;
        }

        public AgeItemTraits build() {
            return new AgeItemTraits(this.beforeDone, this.afterDone);
        }
    }
}
