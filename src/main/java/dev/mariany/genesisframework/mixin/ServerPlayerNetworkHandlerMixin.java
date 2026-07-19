package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.advancement.criterion.GFCriteria;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayerNetworkHandlerMixin {
    @Shadow
    public ServerPlayer player;

    /**
     * Triggers the {@link GFCriteria#OPEN_ADVANCEMENT_TAB} criteria when removing player from Trial Spawner state.
     */
    @Inject(method = "handleSeenAdvancements", at = @At(value = "TAIL"))
    public void injectOnAdvancementTab(ServerboundSeenAdvancementsPacket packet, CallbackInfo ci) {
        MinecraftServer server = player.level().getServer();

        if (packet.getAction() == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
            Identifier advancementId = Objects.requireNonNull(packet.getTab());
            AdvancementHolder advancementEntry = server.getAdvancements().get(advancementId);

            if (advancementEntry != null) {
                GFCriteria.OPEN_ADVANCEMENT_TAB.trigger(player, advancementId);
            }
        }
    }
}
