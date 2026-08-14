package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.event.server.level.TrialSpawnerEvents;
import dev.mariany.genesisframework.mixin.accessor.TrialSpawnerDataAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.UUID;

@Mixin(TrialSpawnerState.class)
public class TrialSpawnerStateMixin {
    /**
     * Invokes {@link TrialSpawnerEvents#COMPLETED} when the trial spawner ejects its reward.
     */
    @Inject(
            method = "tickAndGetNext",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"
            )
    )
    private void injectTickAndGetNext(
            BlockPos pos,
            TrialSpawner logic,
            ServerLevel level,
            CallbackInfoReturnable<TrialSpawnerState> cir
    ) {
        TrialSpawnerStateData data = logic.getStateData();
        Set<UUID> players = ((TrialSpawnerDataAccessor) data).genesis$players();
        TrialSpawnerEvents.COMPLETED.invoker().onCompleted(level, players, logic.isOminous());
    }
}
