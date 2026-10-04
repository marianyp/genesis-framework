package dev.mariany.genesisframework.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CompleteTrialSpawnerTrigger extends SimpleCriterionTrigger<CompleteTrialSpawnerTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, boolean ominous) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(ominous));
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player, boolean expectOminous)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                            LootItemCondition.CODEC
                                                    .optionalFieldOf("player")
                                                    .forGetter(TriggerInstance::player),
                                            Codec.BOOL.fieldOf("ominous")
                                                      .forGetter(TriggerInstance::expectOminous)
                                    )
                                    .apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> create(boolean ominous) {
            return create(null, ominous);
        }

        public static Criterion<TriggerInstance> create(
                @Nullable Holder<LootItemCondition> playerPredicate,
                boolean ominous
        ) {
            return GFCriteriaTriggers.COMPLETE_TRIAL_SPAWNER_ADVANCEMENT.createCriterion(
                    new TriggerInstance(Optional.ofNullable(playerPredicate), ominous)
            );
        }

        public boolean matches(boolean ominous) {
            return ominous == expectOminous;
        }
    }
}
