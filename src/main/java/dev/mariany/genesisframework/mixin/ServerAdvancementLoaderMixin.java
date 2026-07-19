package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

@Mixin(ServerAdvancementManager.class)
public class ServerAdvancementLoaderMixin {
    /**
     * Invokes the {@link ServerAdvancementEvents#BEFORE_ADVANCEMENTS_LOAD} event during advancements load.
     */
    @Inject(
            at = @At("HEAD"),
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V"
    )
    protected void apply(
            Map<Identifier, Advancement> map,
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfo ci
    ) {
        ServerAdvancementEvents.BEFORE_ADVANCEMENTS_LOAD.invoker().onAdvancementsLoaded(map);
    }
}
