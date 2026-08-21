package dev.mariany.genesisframework.advancement.criterion;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class OpenAdvancementTabTriggerHandler {
    private OpenAdvancementTabTriggerHandler() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Open Advancement Tab Trigger Handler");
        ServerAdvancementEvents.TAB_ACTION.register(OpenAdvancementTabTriggerHandler::onAdvancementTabOpened);
    }

    private static void onAdvancementTabOpened(ServerPlayer player, ServerboundSeenAdvancementsPacket packet) {
        if (packet.getAction() != ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
            return;
        }

        MinecraftServer server = player.level().getServer();
        Identifier advancementId = packet.getTab();

        Identifier validatedAdvancementId = advancementId == null || isUnknownAdvancement(server, advancementId) ?
                null :
                advancementId;

        GFCriteriaTriggers.OPEN_ADVANCEMENT_TAB.trigger(player, validatedAdvancementId);
    }

    private static boolean isUnknownAdvancement(MinecraftServer server, Identifier advancementId) {
        return server.getAdvancements().get(advancementId) == null;
    }
}