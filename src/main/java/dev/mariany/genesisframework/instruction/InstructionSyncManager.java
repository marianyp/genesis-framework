package dev.mariany.genesisframework.instruction;

import dev.mariany.genesisframework.packet.clientbound.UpdateInstructionsPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class InstructionSyncManager {
    public static void syncInstructions(ServerPlayer player) {
        List<Identifier> instructions = ServerInstructionManager.getInstance().getInstructionAdvancementIds();
        syncInstructions(player, instructions);
    }

    public static void syncInstructions(ServerPlayer player, List<Identifier> instructions) {
        ServerPlayNetworking.send(player, new UpdateInstructionsPayload(instructions));
    }
}
