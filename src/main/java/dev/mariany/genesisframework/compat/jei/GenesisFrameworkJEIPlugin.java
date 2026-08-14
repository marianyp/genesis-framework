package dev.mariany.genesisframework.compat.jei;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.client.GenesisFrameworkClient;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@JeiPlugin
public class GenesisFrameworkJEIPlugin implements IModPlugin {
    private static final Identifier PLUGIN_ID = GenesisFramework.id("jei_plugin");

    private final JEIAgeRequirementManager ageRequirementManager = new JEIAgeRequirementManager();
    private final Runnable itemRestrictionListener = this::scheduleViewRefresh;

    @Nullable
    private IJeiRuntime runtime;

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        this.ageRequirementManager.registerCategory(registration);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        this.ageRequirementManager.registerViews(registration);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        this.runtime = runtime;

        JEIAgeTransferHandler.bootstrap();

        GenesisFrameworkClient
                .getAgeManager()
                .addItemRestrictionListener(this.itemRestrictionListener);

        this.scheduleViewRefresh();
    }

    @Override
    public void onRuntimeUnavailable() {
        GenesisFrameworkClient.getAgeManager().removeItemRestrictionListener(this.itemRestrictionListener);
        this.runtime = null;
    }

    private void scheduleViewRefresh() {
        Minecraft.getInstance().execute(this::refreshViews);
    }

    private void refreshViews() {
        if (this.runtime == null) {
            return;
        }

        this.refreshViews(this.runtime.getRecipeManager());
    }

    private void refreshViews(IRecipeManager recipeManager) {
        this.ageRequirementManager.refreshViews(recipeManager);
    }
}
