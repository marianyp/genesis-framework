package dev.mariany.genesisframework.client;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.client.age.ClientAgeManager;
import dev.mariany.genesisframework.client.instruction.ClientInstructionManager;
import dev.mariany.genesisframework.config.ConfigHandler;
import dev.mariany.genesisframework.config.GFClientConfig;
import dev.mariany.genesisframework.packet.clientbound.ClientBoundPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

@Environment(EnvType.CLIENT)
public class GFClient implements ClientModInitializer {
    private static final ConfigHandler<GFClientConfig> CONFIG_HANDLER = new ConfigHandler<>(
            GenesisFramework.MOD_ID + "-client",
            new GFClientConfig()
    );

    public static GFClientConfig getConfig() {
        return CONFIG_HANDLER.getConfig();
    }

    @Override
    public void onInitializeClient() {
        CONFIG_HANDLER.loadConfig();
        ClientBoundPackets.init();
        ClientPlayConnectionEvents.INIT.register(GFClient::cleanup);
        ClientPlayConnectionEvents.DISCONNECT.register(GFClient::cleanup);
    }

    private static void cleanup(
            ClientPacketListener clientPlayNetworkHandler,
            Minecraft minecraftClient
    ) {
        ClientAgeManager.getInstance().reset();
        ClientInstructionManager.getInstance().reset();
    }
}
