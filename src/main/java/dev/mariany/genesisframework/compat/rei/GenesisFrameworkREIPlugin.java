package dev.mariany.genesisframework.compat.rei;

import dev.mariany.genesisframework.client.age.requirement.view.AgeRequirementView;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;

public class GenesisFrameworkREIPlugin implements REIClientPlugin {
    private final AgeRequirementView ageRequirementView = new AgeRequirementView(true);

    private final CategoryIdentifier<REIAgeRequirementDisplay> categoryIdentifier = CategoryIdentifier.of(
            this.ageRequirementView.id()
    );

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new REIAgeRequirementCategory(this.ageRequirementView, this.categoryIdentifier));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerDisplayGenerator(
                this.categoryIdentifier,
                new REIAgeRequirementDisplayGenerator(this.categoryIdentifier)
        );
    }

    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(new REIAgeTransferHandler());
    }
}
