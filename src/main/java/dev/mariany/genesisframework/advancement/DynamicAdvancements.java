package dev.mariany.genesisframework.advancement;

import com.google.common.collect.ImmutableMap;
import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import dev.mariany.genesisframework.mixin.accessor.ServerAdvancementManagerAccessor;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.util.ProblemReporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DynamicAdvancements {
    private final List<Supplier<? extends Collection<AdvancementHolder>>> advancementProviders = new ArrayList<>();
    private final ThreadLocal<Boolean> treeBuildDeferred = ThreadLocal.withInitial(() -> false);

    public void bootstrap() {
        GenesisFramework.bootstrapLog("Dynamic Advancements");
        ServerAdvancementEvents.ALLOW_TREE_BUILD.register(_ -> !this.treeBuildDeferred.get());
        ServerAdvancementEvents.CREATE_MANAGER.register(this::onCreateManager);
        ServerAdvancementEvents.FINALIZE_RELOAD_MANAGER.register(this::onFinalizeReloadManager);
    }

    private ServerAdvancementManager onCreateManager(
            HolderLookup.Provider registries,
            ServerAdvancementEvents.AdvancementManagerFactory factory
    ) {
        boolean previousTreeBuildDeferred = this.treeBuildDeferred.get();

        this.treeBuildDeferred.set(true);

        try {
            return factory.create(registries);
        } finally {
            if (previousTreeBuildDeferred) {
                this.treeBuildDeferred.set(true);
            } else {
                this.treeBuildDeferred.remove();
            }
        }
    }

    private void onFinalizeReloadManager(
            ServerAdvancementManager manager,
            HolderLookup.Provider registries
    ) {
        Map<Identifier, AdvancementHolder> updatedAdvancements = this.addTo(getAdvancements(manager), registries);

        updateAdvancements(manager, updatedAdvancements);

        AdvancementTree tree = getAdvancementTree(manager);

        tree.addAll(updatedAdvancements.values());
        tree.repositionNodes();
    }

    private static Map<Identifier, AdvancementHolder> getAdvancements(ServerAdvancementManager manager) {
        return ((ServerAdvancementManagerAccessor) manager).genesis$advancements();
    }

    private static void updateAdvancements(
            ServerAdvancementManager manager,
            Map<Identifier, AdvancementHolder> advancements
    ) {
        ((ServerAdvancementManagerAccessor) manager).genesis$advancements(advancements);
    }

    private static AdvancementTree getAdvancementTree(ServerAdvancementManager manager) {
        return ((ServerAdvancementManagerAccessor) manager).genesis$tree();
    }

    public ImmutableMap<Identifier, AdvancementHolder> addTo(
            Map<Identifier, AdvancementHolder> loadedAdvancements,
            HolderLookup.Provider registries
    ) {
        List<AdvancementHolder> dynamicAdvancements = this.advancementProviders
                .stream()
                .map(Supplier::get)
                .flatMap(Collection::stream)
                .toList();

        Map<Identifier, AdvancementHolder> advancements = new LinkedHashMap<>(loadedAdvancements);

        for (AdvancementHolder advancement : dynamicAdvancements) {
            validate(advancement, registries);
            advancements.put(advancement.id(), advancement);
        }

        GenesisFramework.LOGGER.info("Dynamically added {} advancements successfully!", dynamicAdvancements.size());

        return ImmutableMap.copyOf(advancements);
    }

    private static void validate(AdvancementHolder advancement, HolderLookup.Provider registries) {
        ProblemReporter.Collector problemReporter = new ProblemReporter.Collector();
        advancement.value().validate(problemReporter, registries);

        if (!problemReporter.isEmpty()) {
            GenesisFramework.LOGGER.warn(
                    "Found validation problems in generated advancement {}: \n{}",
                    advancement.id(),
                    problemReporter.getReport()
            );
        }
    }

    public void register(Supplier<AdvancementHolder> advancementFactory) {
        this.registerAll(() -> List.of(advancementFactory.get()));
    }

    public void registerAll(Supplier<? extends Collection<AdvancementHolder>> advancementProvider) {
        this.advancementProviders.add(advancementProvider);
    }
}
