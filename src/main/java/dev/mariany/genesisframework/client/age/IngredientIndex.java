package dev.mariany.genesisframework.client.age;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class IngredientIndex {
    public static final IngredientIndex EMPTY = new IngredientIndex(Map.of(), List.of(), List.of());

    private final Map<Item, List<AgeIngredient>> ingredientsByItem;
    private final List<AgeIngredient> ingredientsRequiringTesting;
    private final List<Ingredient> ingredients;

    private IngredientIndex(
            Map<Item, List<AgeIngredient>> ingredientsByItem,
            List<AgeIngredient> ingredientsRequiringTesting,
            List<Ingredient> ingredients
    ) {
        this.ingredientsByItem = ingredientsByItem;
        this.ingredientsRequiringTesting = ingredientsRequiringTesting;
        this.ingredients = ingredients;
    }

    public static IngredientIndex create(Map<Identifier, List<Ingredient>> ingredientsByAge) {
        Map<Item, List<AgeIngredient>> ingredientsByItem = new HashMap<>();
        List<AgeIngredient> ingredientsRequiringTesting = new ArrayList<>();
        List<Ingredient> allIngredients = new ArrayList<>();

        ingredientsByAge.forEach(
                (ageId, ingredients) -> ingredients.forEach(ingredient -> {
                    AgeIngredient ageIngredient = new AgeIngredient(ageId, ingredient);

                    allIngredients.add(ingredient);

                    if (ingredient.requiresTesting()) {
                        ingredientsRequiringTesting.add(ageIngredient);
                        return;
                    }

                    ingredient.items().forEach(item -> ingredientsByItem
                            .computeIfAbsent(item.value(), ignored -> new ArrayList<>())
                            .add(ageIngredient));
                })
        );

        ingredientsByItem.replaceAll((_, ingredients) -> List.copyOf(ingredients));

        return new IngredientIndex(
                Map.copyOf(ingredientsByItem),
                List.copyOf(ingredientsRequiringTesting),
                List.copyOf(allIngredients)
        );
    }

    public boolean matches(ItemStack stack) {
        return this.getCandidates(stack).anyMatch(candidate -> candidate.ingredient().test(stack));
    }

    public Set<Identifier> getMatchingAgeIds(ItemStack stack) {
        return this.getCandidates(stack)
                   .filter(candidate -> candidate.ingredient().test(stack))
                   .map(AgeIngredient::ageId)
                   .collect(Collectors.toSet());
    }

    public void forEachIngredient(Consumer<Ingredient> consumer) {
        this.ingredients.forEach(consumer);
    }

    private Stream<AgeIngredient> getCandidates(ItemStack stack) {
        return Stream.concat(
                this.ingredientsByItem.getOrDefault(stack.getItem(), List.of()).stream(),
                this.ingredientsRequiringTesting.stream()
        );
    }

    private record AgeIngredient(Identifier ageId, Ingredient ingredient) {
    }
}