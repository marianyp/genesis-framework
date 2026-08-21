package dev.mariany.genesisframework.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ItemBrokenTrigger extends SimpleCriterionTrigger<ItemBrokenTrigger.Conditions> {
    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack stack) {
        this.trigger(player, conditions -> conditions.matches(stack));
    }

    public record Conditions(Optional<ContextAwarePredicate> player, Ingredient ingredient)
            implements SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                            EntityPredicate.ADVANCEMENT_CODEC
                                                    .optionalFieldOf("player")
                                                    .forGetter(Conditions::player),
                                            Ingredient.CODEC.fieldOf("item").forGetter(Conditions::ingredient)
                                    )
                                    .apply(instance, Conditions::new)
        );

        public static Criterion<Conditions> create(ItemLike item) {
            return create(null, Ingredient.of(item));
        }

        public static Criterion<Conditions> create(ItemLike... items) {
            return create(null, Ingredient.of(items));
        }

        public static Criterion<Conditions> create(HolderSet<Item> tag) {
            return create(null, Ingredient.of(tag));
        }

        public static Criterion<Conditions> create(Ingredient ingredient) {
            return create(null, ingredient);
        }

        public static Criterion<Conditions> create(
                @Nullable ContextAwarePredicate playerPredicate,
                Ingredient ingredient
        ) {
            return GFCriteriaTriggers.ITEM_BROKEN.createCriterion(new Conditions(
                    Optional.ofNullable(playerPredicate),
                    ingredient
            ));
        }

        public boolean matches(ItemStack stack) {
            return this.ingredient.test(stack);
        }
    }

}