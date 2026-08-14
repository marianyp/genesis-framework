package dev.mariany.genesisframework.advancement;

import dev.mariany.genesisframework.GenesisFramework;
import net.minecraft.advancements.AdvancementHolder;

import java.util.List;

public abstract class DynamicAdvancementManager {
    public void bootstrap() {
        GenesisFramework.DYNAMIC_ADVANCEMENTS.register(this::createRootAdvancement);
        GenesisFramework.DYNAMIC_ADVANCEMENTS.registerAll(this::getAdvancements);
    }

    protected abstract AdvancementHolder createRootAdvancement();

    protected abstract List<AdvancementHolder> getAdvancements();
}
