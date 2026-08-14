package dev.mariany.genesisframework.compat.rei;

import dev.mariany.genesisframework.client.age.requirement.AgeRequirementData;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record REIAgeRequirementDisplay(
        AgeRequirementData view,
        CategoryIdentifier<REIAgeRequirementDisplay> categoryIdentifier
) implements Display {
    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of();
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(EntryIngredients.of(this.view.stack()));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return this.categoryIdentifier;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return Optional.empty();
    }

    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return null;
    }
}
