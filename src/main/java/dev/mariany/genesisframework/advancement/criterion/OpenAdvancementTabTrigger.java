package dev.mariany.genesisframework.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class OpenAdvancementTabTrigger extends SimpleCriterionTrigger<OpenAdvancementTabTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, @Nullable Identifier tab) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(tab));
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player, Optional<Identifier> advancementId)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                            LootItemCondition.CODEC
                                                    .optionalFieldOf("player")
                                                    .forGetter(TriggerInstance::player),
                                            Identifier.CODEC.optionalFieldOf("advancement")
                                                            .forGetter(TriggerInstance::advancementId)
                                    )
                                    .apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> create() {
            return create(null, null);
        }

        public static Criterion<TriggerInstance> create(Identifier tab) {
            return create(null, tab);
        }

        public static Criterion<TriggerInstance> create(
                @Nullable Holder<LootItemCondition> playerPredicate,
                @Nullable Identifier tab
        ) {
            return GFCriteriaTriggers.OPEN_ADVANCEMENT_TAB.createCriterion(
                    new TriggerInstance(Optional.ofNullable(playerPredicate), Optional.ofNullable(tab))
            );
        }

        public boolean matches(@Nullable Identifier tab) {
            return this.advancementId.map(identifier -> identifier.equals(tab)).orElse(true);
        }
    }
}
