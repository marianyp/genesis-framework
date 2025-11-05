package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.age.Age;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.age.AgeManager;
import dev.mariany.genesisframework.age.AgeShareManager;
import dev.mariany.genesisframework.config.ConfigHandler;
import dev.mariany.genesisframework.gamerule.GFGamerules;
import dev.mariany.genesisframework.packet.clientbound.UpdateLockedItems;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
    @Shadow
    private ServerPlayerEntity owner;

    /**
     * Prevent earning an age's advancement criterion if the parent age is not complete.
     */
    @Inject(method = "grantCriterion", at = @At(value = "HEAD"), cancellable = true)
    public void injectGrantCriterion(
            AdvancementEntry advancement,
            String criterionName,
            CallbackInfoReturnable<Boolean> cir
    ) {
        AgeManager ageManager = AgeManager.getInstance();
        Optional<AgeEntry> optionalAgeEntry = ageManager.find(advancement);

        if (optionalAgeEntry.isPresent()) {
            AgeEntry ageEntry = optionalAgeEntry.get();
            Age age = ageEntry.getAge();
            Optional<Identifier> optionalParentId = age.parent();

            if (age.requiresParent() && optionalParentId.isPresent()) {
                while (optionalParentId.isPresent()) {
                    Optional<AgeEntry> optionalParent = ageManager.get(optionalParentId.get());

                    if (optionalParent.isPresent()) {
                        AgeEntry parentAgeEntry = optionalParent.get();
                        Age parentAge = parentAgeEntry.getAge();

                        if (!parentAge.requiresParent()) {
                            optionalParentId = parentAge.parent();
                        } else {
                            if (!parentAgeEntry.isDone(owner)) {
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
     * Updates client age item unlocks state and shares the age advancement with other players.
     */
    @WrapOperation(
            method = "grantCriterion", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancement/PlayerAdvancementTracker;onStatusUpdate(Lnet/minecraft/advancement/AdvancementEntry;)V"
    )
    )
    public void wrapOnStatusUpdate(
            PlayerAdvancementTracker playerAdvancementTracker,
            AdvancementEntry advancement,
            Operation<Void> original
    ) {
        AgeManager ageManager = AgeManager.getInstance();
        Optional<AgeEntry> optionalAge = ageManager.find(advancement);

        if (optionalAge.isPresent()) {
            ServerPlayNetworking.send(owner, new UpdateLockedItems(ageManager.getLockedItems(owner)));

            MinecraftServer server = owner.getEntityWorld().getServer();

            if (server.getGameRules().getBoolean(GFGamerules.SHARED_AGE_PROGRESSION)) {
                AgeShareManager ageShareManager = AgeShareManager.getServerState(server);

                if (ConfigHandler.getConfig().teamBasedAgeSharing) {
                    ageShareManager.shareWithTeam(owner, optionalAge.get());
                } else {
                    ageShareManager.shareWithServer(server, optionalAge.get());
                }
            }
        }

        original.call(playerAdvancementTracker, advancement);
    }
}
