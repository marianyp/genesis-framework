package dev.mariany.genesisframework.client;

import dev.mariany.genesisframework.client.age.ClientAgeManager;
import dev.mariany.genesisframework.client.age.ClientAgeRecipeRestrictions;
import dev.mariany.genesisframework.client.advancement.ClientAdvancementDisplay;
import dev.mariany.genesisframework.client.instruction.ClientInstructionManager;
import dev.mariany.genesisframework.client.recipe.RecipeButtonFixes;
import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.config.ConfigHandler;
import dev.mariany.genesisframework.config.GFClientConfig;
import dev.mariany.genesisframework.packet.clientbound.ClientBoundPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GenesisFrameworkClient implements ClientModInitializer {
    private static final ConfigHandler<GFClientConfig> CONFIG_HANDLER = new ConfigHandler<>(
            GenesisFramework.MOD_ID + "-client",
            new GFClientConfig()
    );

    private static final ClientSessionState SESSION_STATE = new ClientSessionState();

    public static GFClientConfig getConfig() {
        return GenesisFrameworkClient.CONFIG_HANDLER.getConfig();
    }

    public static ClientAgeManager getAgeManager() {
        return GenesisFrameworkClient.SESSION_STATE.getAgeManager();
    }

    public static ClientInstructionManager getInstructionManager() {
        return GenesisFrameworkClient.SESSION_STATE.getInstructionManager();
    }

    @Override
    public void onInitializeClient() {
        GenesisFrameworkClient.CONFIG_HANDLER.loadConfig();
        ClientBoundPackets.bootstrap();
        ClientAgeRecipeRestrictions.bootstrap();
        ClientAdvancementDisplay.bootstrap();
        RecipeButtonFixes.bootstrap();
        GenesisFrameworkClient.SESSION_STATE.bootstrap();
    }
}
