package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
    @Shadow
    private ServerPlayer player;

    /**
     * Invokes {@link ServerAdvancementEvents#COMPLETION_UPDATED} when advancement completion is marked for update.
     */
    @Inject(method = "markForVisibilityUpdate", at = @At(value = "TAIL"))
    public void injectMarkForVisibilityUpdate(AdvancementHolder advancement, CallbackInfo ci) {
        ServerAdvancementEvents.COMPLETION_UPDATED.invoker().onCompletionUpdated(this.player, advancement);
    }

    /**
     * Queries {@link ServerAdvancementEvents#ALLOW_AWARD} before awarding an advancement criterion.
     */
    @Inject(method = "award", at = @At(value = "HEAD"), cancellable = true)
    public void injectAwardHead(
            AdvancementHolder advancement,
            String criterionName,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (ServerAdvancementEvents.ALLOW_AWARD.invoker().allowAward(this.player, advancement)) {
            return;
        }

        cir.setReturnValue(false);
    }

    /**
     * Invokes {@link ServerAdvancementEvents#AWARDED} after attempting to award an advancement criterion.
     */
    @Inject(method = "award", at = @At(value = "TAIL"))
    public void injectAwardTail(AdvancementHolder advancement, String criterion, CallbackInfoReturnable<Boolean> cir) {
        ServerAdvancementEvents.AWARDED.invoker().onAwarded(this.player, advancement);
    }
}
