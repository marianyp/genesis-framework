package dev.mariany.genesisframework.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class CompleteTrialSpawnerCriteria extends SimpleCriterionTrigger<CompleteTrialSpawnerCriteria.Conditions> {
    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, boolean ominous) {
        this.trigger(player, conditions -> conditions.matches(ominous));
    }

    public record Conditions(Optional<ContextAwarePredicate> player, boolean expectOminous)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.ADVANCEMENT_CODEC
                                        .optionalFieldOf("player")
                                        .forGetter(Conditions::player),
                                Codec.BOOL.fieldOf("ominous")
                                        .forGetter(Conditions::expectOminous)
                        )
                        .apply(instance, Conditions::new)
        );

        public static Criterion<Conditions> create(boolean ominous) {
            return create(null, ominous);
        }

        public static Criterion<Conditions> create(
                @Nullable ContextAwarePredicate playerPredicate,
                boolean ominous
        ) {
            return GFCriteria.COMPLETE_TRIAL_SPAWNER_ADVANCEMENT.createCriterion(
                    new Conditions(Optional.ofNullable(playerPredicate), ominous)
            );
        }

        public boolean matches(boolean ominous) {
            return ominous == expectOminous;
        }
    }
}
