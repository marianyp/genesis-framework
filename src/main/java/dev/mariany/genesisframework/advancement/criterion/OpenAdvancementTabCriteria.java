package dev.mariany.genesisframework.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class OpenAdvancementTabCriteria extends SimpleCriterionTrigger<OpenAdvancementTabCriteria.Conditions> {
    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, Identifier tab) {
        this.trigger(player, conditions -> conditions.matches(tab));
    }

    public record Conditions(Optional<ContextAwarePredicate> player, Optional<Identifier> advancementId)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.ADVANCEMENT_CODEC
                                        .optionalFieldOf("player")
                                        .forGetter(Conditions::player),
                                Identifier.CODEC.optionalFieldOf("advancement")
                                        .forGetter(Conditions::advancementId)
                        )
                        .apply(instance, Conditions::new)
        );

        public static Criterion<Conditions> create() {
            return create(null, null);
        }

        public static Criterion<Conditions> create(Identifier tab) {
            return create(null, tab);
        }

        public static Criterion<Conditions> create(
                @Nullable ContextAwarePredicate playerPredicate,
                @Nullable Identifier tab
        ) {
            return GFCriteria.OPEN_ADVANCEMENT_TAB.createCriterion(
                    new Conditions(
                            Optional.ofNullable(playerPredicate),
                            Optional.ofNullable(tab)
                    )
            );
        }

        public boolean matches(Identifier tab) {
            return advancementId.map(identifier -> identifier.equals(tab)).orElse(true);
        }
    }
}
