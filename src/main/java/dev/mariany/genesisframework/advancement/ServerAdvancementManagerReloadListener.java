package dev.mariany.genesisframework.advancement;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.Unit;

public class ServerAdvancementManagerReloadListener extends SimpleReloadListener<Unit> {
    private final ServerAdvancementManager manager;

    public ServerAdvancementManagerReloadListener(ServerAdvancementManager manager) {
        this.manager = manager;
    }

    @Override
    protected Unit prepare(PreparableReloadListener.SharedState state) {
        return Unit.INSTANCE;
    }

    @Override
    protected void apply(Unit prepared, PreparableReloadListener.SharedState state) {
        ServerAdvancementEvents.FINALIZE_RELOAD_MANAGER.invoker().onFinalizeReloadManager(
                this.manager,
                state.get(ResourceLoader.REGISTRY_LOOKUP_KEY)
        );
    }
}
