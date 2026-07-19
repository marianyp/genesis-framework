package dev.mariany.genesisframework;

import dev.mariany.genesisframework.advancement.criterion.GFCriteria;
import dev.mariany.genesisframework.age.AgeShareManager;
import dev.mariany.genesisframework.component.GFComponentTypes;
import dev.mariany.genesisframework.event.block.UseBlockHandler;
import dev.mariany.genesisframework.event.server.SyncDataPackContentsHandler;
import dev.mariany.genesisframework.event.server.advancement.BeforeAdvancementsLoadHandler;
import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import dev.mariany.genesisframework.event.server.command.CommandRegistrationHandler;
import dev.mariany.genesisframework.gamerule.GFGameRules;
import dev.mariany.genesisframework.item.GFItems;
import dev.mariany.genesisframework.packet.GFPackets;
import dev.mariany.genesisframework.sound.GFSoundEvents;
import dev.mariany.genesisframework.stat.GFStats;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenesisFramework implements ModInitializer {
    public static final String MOD_ID = "genesisframework";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String resource) {
        return Identifier.fromNamespaceAndPath(MOD_ID, resource);
    }

    public static void bootstrapLog(String type) {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return;
        }

        LOGGER.info("Registering {}", type);
    }

    @Override
    public void onInitialize() {
        GFPackets.bootstrap();
        GFComponentTypes.bootstrap();
        GFSoundEvents.bootstrap();
        GFStats.bootstrap();
        GFCriteria.bootstrap();
        GFItems.bootstrap();
        GFGameRules.bootstrap();
        AgeShareManager.bootstrap();

        UseBlockCallback.EVENT.register(UseBlockHandler::onUseBlock);
        ServerAdvancementEvents.BEFORE_ADVANCEMENTS_LOAD.register(BeforeAdvancementsLoadHandler::beforeAdvancementsLoad);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(SyncDataPackContentsHandler::onSyncDataPackContents);
        CommandRegistrationCallback.EVENT.register(CommandRegistrationHandler::onRegister);
    }
}