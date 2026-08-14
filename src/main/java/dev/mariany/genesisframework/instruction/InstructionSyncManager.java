package dev.mariany.genesisframework.instruction;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.packet.clientbound.UpdateInstructionsPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class InstructionSyncManager {
    private InstructionSyncManager() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Instruction Sync Manager");
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(InstructionSyncManager::syncDataPackContents);
    }

    private static void syncDataPackContents(ServerPlayer player, boolean joined) {
        syncInstructions(player);
    }

    public static void syncInstructions(ServerPlayer player) {
        List<Identifier> instructions = GenesisFramework.getServerInstructionManager().getInstructionAdvancementIds();
        syncInstructions(player, instructions);
    }

    public static void syncInstructions(ServerPlayer player, List<Identifier> instructions) {
        ServerPlayNetworking.send(player, new UpdateInstructionsPayload(instructions));
    }
}
