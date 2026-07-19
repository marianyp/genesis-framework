package dev.mariany.genesisframework.event.server;

import dev.mariany.genesisframework.age.AgeSyncManager;
import dev.mariany.genesisframework.instruction.InstructionSyncManager;
import net.minecraft.server.level.ServerPlayer;

public class SyncDataPackContentsHandler {
    public static void onSyncDataPackContents(ServerPlayer player, boolean joined) {
        AgeSyncManager.syncLockedItems(player);
        AgeSyncManager.syncItemTraits(player);
        InstructionSyncManager.syncInstructions(player);
    }
}
