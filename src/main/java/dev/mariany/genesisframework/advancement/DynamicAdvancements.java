package dev.mariany.genesisframework.advancement;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DynamicAdvancements {
    private final List<Supplier<? extends Collection<AdvancementHolder>>> advancementProviders = new ArrayList<>();

    public void bootstrap() {
        ServerAdvancementEvents.BEFORE_ADVANCEMENTS_LOAD.register(this::addTo);
    }

    protected void addTo(Map<Identifier, Advancement> advancementMap) {
        List<AdvancementHolder> advancements = this.advancementProviders
                .stream()
                .map(Supplier::get)
                .flatMap(Collection::stream)
                .toList();

        for (AdvancementHolder advancement : advancements) {
            advancementMap.put(advancement.id(), advancement.value());
        }

        GenesisFramework.LOGGER.info("Dynamically added {} advancements successfully!", advancements.size());
    }

    public void register(Supplier<AdvancementHolder> advancementFactory) {
        this.registerAll(() -> List.of(advancementFactory.get()));
    }

    public void registerAll(Supplier<? extends Collection<AdvancementHolder>> advancementProvider) {
        this.advancementProviders.add(advancementProvider);
    }
}
