package dev.mariany.genesisframework;

import dev.mariany.genesisframework.advancement.AdvancementVisibilityHandler;
import dev.mariany.genesisframework.advancement.DynamicAdvancements;
import dev.mariany.genesisframework.advancement.criterion.CompleteTrialSpawnerTriggerHandler;
import dev.mariany.genesisframework.advancement.criterion.GFCriteriaTriggers;
import dev.mariany.genesisframework.advancement.criterion.ItemBrokenTriggerHandler;
import dev.mariany.genesisframework.advancement.criterion.OpenAdvancementTabTriggerHandler;
import dev.mariany.genesisframework.age.AgeShareManager;
import dev.mariany.genesisframework.age.AgeSyncManager;
import dev.mariany.genesisframework.age.ServerAgeManager;
import dev.mariany.genesisframework.age.restriction.AgeBlockRestrictions;
import dev.mariany.genesisframework.age.restriction.AgePortalRestrictions;
import dev.mariany.genesisframework.age.restriction.AgeRecipeRestrictions;
import dev.mariany.genesisframework.component.GFComponentTypes;
import dev.mariany.genesisframework.gamerule.GFGameRules;
import dev.mariany.genesisframework.instruction.InstructionSyncManager;
import dev.mariany.genesisframework.instruction.ServerInstructionManager;
import dev.mariany.genesisframework.item.GFItems;
import dev.mariany.genesisframework.packet.GFPackets;
import dev.mariany.genesisframework.registry.GFDataResources;
import dev.mariany.genesisframework.server.command.AgeCommand;
import dev.mariany.genesisframework.server.command.GFCommands;
import dev.mariany.genesisframework.sound.GFSoundEvents;
import dev.mariany.genesisframework.stat.GFStats;
import dev.mariany.genesisframework.stat.HostileKillsStatHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenesisFramework implements ModInitializer {
    public static final String MOD_ID = "genesisframework";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final ServerAgeManager AGE_MANAGER = new ServerAgeManager();
    private static final ServerInstructionManager INSTRUCTION_MANAGER = new ServerInstructionManager();

    public static final DynamicAdvancements DYNAMIC_ADVANCEMENTS = new DynamicAdvancements();

    public static ServerAgeManager getServerAgeManager() {
        return GenesisFramework.AGE_MANAGER;
    }

    public static ServerInstructionManager getServerInstructionManager() {
        return GenesisFramework.INSTRUCTION_MANAGER;
    }

    public static Identifier id(String resource) {
        return Identifier.fromNamespaceAndPath(MOD_ID, resource);
    }

    public static void bootstrapLog(String type) {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return;
        }

        GenesisFramework.LOGGER.info("Registering {}", type);
    }

    @Override
    public void onInitialize() {
        GFDataResources.bootstrap();
        GFPackets.bootstrap();
        GFComponentTypes.bootstrap();
        GFSoundEvents.bootstrap();
        GFStats.bootstrap();
        HostileKillsStatHandler.bootstrap();
        GFCriteriaTriggers.bootstrap();
        CompleteTrialSpawnerTriggerHandler.bootstrap();
        ItemBrokenTriggerHandler.bootstrap();
        OpenAdvancementTabTriggerHandler.bootstrap();
        GFItems.bootstrap();
        GFGameRules.bootstrap();
        AgeShareManager.bootstrap();
        AgeBlockRestrictions.bootstrap();
        AgePortalRestrictions.bootstrap();
        AgeRecipeRestrictions.bootstrap();
        AdvancementVisibilityHandler.bootstrap();
        AgeSyncManager.bootstrap();
        InstructionSyncManager.bootstrap();
        GenesisFramework.AGE_MANAGER.bootstrap();
        GenesisFramework.INSTRUCTION_MANAGER.bootstrap();
        GenesisFramework.DYNAMIC_ADVANCEMENTS.bootstrap();
        AgeCommand.bootstrap();
        GFCommands.bootstrap();
    }
}
