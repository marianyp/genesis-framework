package dev.mariany.genesisframework.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import java.util.UUID;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;

@Mixin(TrialSpawnerStateData.class)
public interface TrialSpawnerDataAccessor {
    @Accessor("detectedPlayers")
    Set<UUID> genesis$players();
}
