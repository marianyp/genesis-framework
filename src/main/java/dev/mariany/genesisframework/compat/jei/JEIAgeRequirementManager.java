package dev.mariany.genesisframework.compat.jei;

import dev.mariany.genesisframework.client.GenesisFrameworkClient;
import dev.mariany.genesisframework.client.age.requirement.AgeRequirementData;
import dev.mariany.genesisframework.client.age.requirement.view.AgeRequirementView;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JEIAgeRequirementManager {
    private final AgeRequirementView ageRequirementView;
    private final IRecipeType<AgeRequirementData> recipeType;

    private final Map<AgeRequirementData, RegisteredData> registeredData = new HashMap<>();

    public JEIAgeRequirementManager() {
        this(new AgeRequirementView(true));
    }

    public JEIAgeRequirementManager(AgeRequirementView ageRequirementView) {
        this.ageRequirementView = ageRequirementView;
        this.recipeType = IRecipeType.create(ageRequirementView.id(), AgeRequirementData.class);
    }

    public void registerCategory(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new JEIAgeRequirementCategory(
                        registration.getJeiHelpers().getGuiHelper(),
                        this.recipeType,
                        this.ageRequirementView
                )
        );
    }

    public void refreshViews(IRecipeManager recipeManager) {
        Set<AgeRequirementData> requestedData = new HashSet<>(
                GenesisFrameworkClient.getAgeManager().getAgeRequirements()
        );

        this.hideRemovedData(recipeManager, requestedData);
        this.showRequestedData(recipeManager, requestedData);
    }

    private void hideRemovedData(IRecipeManager recipeManager, Set<AgeRequirementData> requestedData) {
        List<RegisteredData> removedData = this.registeredData
                .values()
                .stream()
                .filter(RegisteredData::visible)
                .filter(data -> !requestedData.contains(data.data()))
                .toList();

        if (removedData.isEmpty()) {
            return;
        }

        recipeManager.hideRecipes(
                this.recipeType,
                removedData.stream().map(RegisteredData::data).toList()
        );

        removedData.forEach(registeredData -> this.registeredData.put(
                registeredData.data(),
                registeredData.withVisible(false)
        ));
    }

    private void showRequestedData(IRecipeManager recipeManager, Set<AgeRequirementData> requestedData) {
        List<AgeRequirementData> addedData = new ArrayList<>();
        List<RegisteredData> restoredData = new ArrayList<>();

        for (AgeRequirementData requested : requestedData) {
            RegisteredData registered = this.registeredData.get(requested);

            if (registered == null) {
                addedData.add(requested);
                continue;
            }

            if (registered.visible()) {
                continue;
            }

            restoredData.add(registered);
        }

        this.restoreData(recipeManager, restoredData);
        this.addData(recipeManager, addedData);
    }

    private void restoreData(IRecipeManager recipeManager, List<RegisteredData> restoredData) {
        if (restoredData.isEmpty()) {
            return;
        }

        recipeManager.unhideRecipes(this.recipeType, restoredData.stream().map(RegisteredData::data).toList());

        restoredData.forEach(registeredData -> this.registeredData.put(
                registeredData.data(),
                registeredData.withVisible(true)
        ));
    }

    private void addData(IRecipeManager recipeManager, List<AgeRequirementData> addedData) {
        if (addedData.isEmpty()) {
            return;
        }

        recipeManager.addRecipes(this.recipeType, addedData);

        addedData.forEach(data -> this.registeredData.put(
                data,
                new RegisteredData(data, true)
        ));
    }

    public void registerViews(IRecipeRegistration registration) {
        List<AgeRequirementData> data = GenesisFrameworkClient.getAgeManager().getAgeRequirements();

        this.registeredData.clear();

        for (AgeRequirementData requirement : data) {
            this.registeredData.put(requirement, new RegisteredData(requirement, true));
        }

        registration.addRecipes(this.recipeType, data);
    }

    private record RegisteredData(AgeRequirementData data, boolean visible) {
        private RegisteredData withVisible(boolean visible) {
            return new RegisteredData(this.data, visible);
        }
    }
}
