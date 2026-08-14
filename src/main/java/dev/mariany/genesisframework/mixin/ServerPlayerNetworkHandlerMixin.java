package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayerNetworkHandlerMixin {
    @Shadow
    public ServerPlayer player;

    /**
     * Invokes {@link ServerAdvancementEvents#TAB_ACTION} after the server handles an advancement-tab action.
     */
    @Inject(method = "handleSeenAdvancements", at = @At(value = "TAIL"))
    public void injectHandleSeenAdvancements(ServerboundSeenAdvancementsPacket packet, CallbackInfo ci) {
        ServerAdvancementEvents.TAB_ACTION.invoker().onTabAction(this.player, packet);
    }
}
