package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.advancement.criterion.GFCriteria;
import dev.mariany.genesisframework.mixin.accessor.TrialSpawnerDataAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@Mixin(TrialSpawnerState.class)
public class TrialSpawnerStateMixin {
    /**
     * Triggers the {@link GFCriteria#COMPLETE_TRIAL_SPAWNER_ADVANCEMENT} criteria when removing player from Trial Spawner state.
     */
    @Inject(
            method = "tickAndGetNext",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"
            )
    )
    private void onEjectReward(
            BlockPos pos,
            TrialSpawner logic,
            ServerLevel level,
            CallbackInfoReturnable<TrialSpawnerState> cir
    ) {
        TrialSpawnerStateData data = logic.getStateData();

        Set<UUID> players = ((TrialSpawnerDataAccessor) data).genesis$players();
        Iterator<UUID> iterator = players.iterator();

        if (iterator.hasNext()) {
            UUID uuid = iterator.next();
            boolean ominous = logic.isOminous();

            Player player = level.getPlayerByUUID(uuid);

            if (player instanceof ServerPlayer serverPlayer) {
                GFCriteria.COMPLETE_TRIAL_SPAWNER_ADVANCEMENT.trigger(serverPlayer, ominous);
            }
        }
    }
}
