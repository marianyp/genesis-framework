package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.age.*;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Optional;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
    @Shadow
    private ServerPlayer player;

    /**
     * Prevent earning an age's advancement criterion if the parent age is not complete.
     */
    @Inject(method = "award", at = @At(value = "HEAD"), cancellable = true)
    public void injectGrantCriterion(
            AdvancementHolder advancement,
            String criterionName,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ServerAgeManager serverAgeManager = ServerAgeManager.getInstance();
        Optional<AgeEntry> optionalAgeEntry = serverAgeManager.find(advancement);

        if (optionalAgeEntry.isPresent()) {
            AgeEntry ageEntry = optionalAgeEntry.get();
            Age age = ageEntry.getAge();
            Optional<Identifier> optionalParentId = age.parent();

            if (age.requiresParent() && optionalParentId.isPresent()) {
                while (optionalParentId.isPresent()) {
                    Optional<AgeEntry> optionalParent = serverAgeManager.get(optionalParentId.get());

                    if (optionalParent.isPresent()) {
                        AgeEntry parentAgeEntry = optionalParent.get();
                        Age parentAge = parentAgeEntry.getAge();

                        if (!parentAge.requiresParent()) {
                            optionalParentId = parentAge.parent();
                        } else {
                            if (!parentAgeEntry.isDone(this.player)) {
                                cir.setReturnValue(false);
                            }
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Update client age state and share age advancement with other players.
     */
    @Inject(
            method = "markForVisibilityUpdate",
            at = @At(value = "TAIL")
    )
    public void injectMarkForVisibilityUpdate(AdvancementHolder advancement, CallbackInfo ci) {
        if (Objects.isNull(this.player.connection) || !this.player.connection.hasClientLoaded()) {
            return;
        }

        ServerAgeManager serverAgeManager = ServerAgeManager.getInstance();
        Optional<AgeEntry> optionalAge = serverAgeManager.find(advancement);

        optionalAge.ifPresent(ageEntry -> {
            AgeSyncManager.syncLockedItems(this.player);
            AgeSyncManager.syncItemTraits(this.player);
            ServerAgeManager.getInstance().onEquipmentUpdate(this.player);
        });
    }

    /**
     * Trigger age sharing after a player is rewarded an advancement.
     */
    @Inject(
            method = "award",
            at = @At(value = "TAIL")
    )
    public void injectAward(AdvancementHolder advancement, String criterion, CallbackInfoReturnable<Boolean> cir) {
        ServerAgeManager serverAgeManager = ServerAgeManager.getInstance();
        Optional<AgeEntry> optionalAge = serverAgeManager.find(advancement);
        optionalAge.ifPresent(ageEntry -> AgeShareManager.onAdvancementAwarded(this.player, ageEntry));
    }
}
