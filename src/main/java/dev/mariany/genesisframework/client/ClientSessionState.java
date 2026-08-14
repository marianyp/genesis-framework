package dev.mariany.genesisframework.client;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.client.age.ClientAgeManager;
import dev.mariany.genesisframework.client.instruction.ClientInstructionManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class ClientSessionState {
    private final ClientAgeManager ageManager = new ClientAgeManager();
    private final ClientInstructionManager instructionManager = new ClientInstructionManager();

    public ClientAgeManager getAgeManager() {
        return this.ageManager;
    }

    public ClientInstructionManager getInstructionManager() {
        return this.instructionManager;
    }

    public void bootstrap() {
        GenesisFramework.bootstrapLog("Client Session State");
        this.ageManager.bootstrap();
        this.instructionManager.bootstrap();
        ClientPlayConnectionEvents.INIT.register(this::onConnectionEvent);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onConnectionEvent);
    }

    protected void onConnectionEvent(ClientPacketListener listener, Minecraft client) {
        this.reset();
    }

    protected void reset() {
        this.ageManager.reset();
        this.instructionManager.reset();
    }
}
