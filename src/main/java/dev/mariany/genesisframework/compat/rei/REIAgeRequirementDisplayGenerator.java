package dev.mariany.genesisframework.compat.rei;

import dev.mariany.genesisframework.client.GenesisFrameworkClient;
import dev.mariany.genesisframework.client.age.requirement.AgeRequirementData;
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class REIAgeRequirementDisplayGenerator
        implements DynamicDisplayGenerator<REIAgeRequirementDisplay> {
    private final CategoryIdentifier<REIAgeRequirementDisplay> categoryIdentifier;

    public REIAgeRequirementDisplayGenerator(CategoryIdentifier<REIAgeRequirementDisplay> categoryIdentifier) {
        this.categoryIdentifier = categoryIdentifier;
    }

    @Override
    public Optional<List<REIAgeRequirementDisplay>> getRecipeFor(EntryStack<?> entry) {
        return displayFor(entry).map(List::of);
    }

    @Override
    public Optional<List<REIAgeRequirementDisplay>> generate(ViewSearchBuilder builder) {
        List<REIAgeRequirementDisplay> displays = GenesisFrameworkClient
                .getAgeManager()
                .getAgeRequirements()
                .stream()
                .map(this::createDisplay)
                .toList();

        if (!builder.getRecipesFor().isEmpty()) {
            displays = filterDisplaysMatchingRecipeOutputs(builder, displays);
        }

        return displays.isEmpty() ? Optional.empty() : Optional.of(displays);
    }

    private static List<REIAgeRequirementDisplay> filterDisplaysMatchingRecipeOutputs(
            ViewSearchBuilder builder,
            List<REIAgeRequirementDisplay> displays
    ) {
        return displays
                .stream()
                .filter(display -> builder
                        .getRecipesFor()
                        .stream()
                        .anyMatch(entry -> display.getOutputEntries()
                                                  .stream()
                                                  .flatMap(List::stream)
                                                  .anyMatch(candidate -> EntryStacks.equalsFuzzy(
                                                          candidate,
                                                          entry
                                                  ))
                        )
                )
                .toList();
    }

    private Optional<REIAgeRequirementDisplay> displayFor(EntryStack<?> entry) {
        if (entry.getType() != VanillaEntryTypes.ITEM) {
            return Optional.empty();
        }

        ItemStack item = entry.<ItemStack>cast().getValue();

        return GenesisFrameworkClient.getAgeManager().getAgeRequirements(item).map(this::createDisplay);
    }

    private REIAgeRequirementDisplay createDisplay(AgeRequirementData view) {
        return new REIAgeRequirementDisplay(view, this.categoryIdentifier);
    }
}
