package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.advancement.criterion.GFCriteria;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayerNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    /**
     * Triggers the {@link GFCriteria#OPEN_ADVANCEMENT_TAB} criteria when removing player from Trial Spawner state.
     */
    @Inject(method = "onAdvancementTab", at = @At(value = "TAIL"))
    public void injectOnAdvancementTab(AdvancementTabC2SPacket packet, CallbackInfo ci) {
        MinecraftServer server = player.getEntityWorld().getServer();

        if (packet.getAction() == AdvancementTabC2SPacket.Action.OPENED_TAB) {
            Identifier advancementId = Objects.requireNonNull(packet.getTabToOpen());
            AdvancementEntry advancementEntry = server.getAdvancementLoader().get(advancementId);

            if (advancementEntry != null) {
                GFCriteria.OPEN_ADVANCEMENT_TAB.trigger(player, advancementId);
            }
        }
    }
}
