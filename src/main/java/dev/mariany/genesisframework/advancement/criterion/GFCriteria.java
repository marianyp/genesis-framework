package dev.mariany.genesisframework.advancement.criterion;

import dev.mariany.genesisframework.GenesisFramework;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class GFCriteria {
    public static final CompleteTrialSpawnerCriteria COMPLETE_TRIAL_SPAWNER_ADVANCEMENT = register(
            "complete_trial_spawner",
            new CompleteTrialSpawnerCriteria()
    );
    public static final OpenAdvancementTabCriteria OPEN_ADVANCEMENT_TAB = register(
            "open_advancement_tab",
            new OpenAdvancementTabCriteria()
    );

    public static <T extends CriterionTrigger<?>> T register(String name, T criterion) {
        return Registry.register(BuiltInRegistries.TRIGGER_TYPES, GenesisFramework.id(name), criterion);
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Criteria");
    }
}
