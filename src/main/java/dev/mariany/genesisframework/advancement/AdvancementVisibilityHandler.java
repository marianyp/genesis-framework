package dev.mariany.genesisframework.advancement;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import net.minecraft.advancements.AdvancementNode;

public final class AdvancementVisibilityHandler {
    private AdvancementVisibilityHandler() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Advancement Visibility Handler");
        ServerAdvancementEvents.SHOULD_FORCE_DISPLAY.register(AdvancementVisibilityHandler::shouldForceDisplay);
    }

    private static boolean shouldForceDisplay(AdvancementNode advancementNode) {
        return isAgeAdvancement(advancementNode) || isInstructionAdvancement(advancementNode);
    }

    private static boolean isAgeAdvancement(AdvancementNode advancementNode) {
        return GenesisFramework.getServerAgeManager().find(advancementNode.holder()).isPresent();
    }

    private static boolean isInstructionAdvancement(AdvancementNode advancementNode) {
        return GenesisFramework.getServerInstructionManager().find(advancementNode.holder()).isPresent();
    }
}
