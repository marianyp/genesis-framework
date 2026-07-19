package dev.mariany.genesisframework.instruction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.entity.PlayerPredicate;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public record Instruction(
        Optional<Identifier> parent,
        Map<String, Criterion<?>> criteria,
        AdvancementRequirements requirements,
        Optional<InstructionDisplay> display
) {
    private static final Codec<Map<String, Criterion<?>>> CRITERIA_CODEC = Codec.unboundedMap(
            Codec.STRING,
            Criterion.CODEC
    );
    public static final Codec<Instruction> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                                        Identifier.CODEC.optionalFieldOf("parent").forGetter(Instruction::parent),
                                        CRITERIA_CODEC.optionalFieldOf("criteria", new HashMap<>()).forGetter(Instruction::criteria),
                                        AdvancementRequirements.CODEC.optionalFieldOf("requirements", AdvancementRequirements.EMPTY)
                                                                     .forGetter(Instruction::requirements),
                                        InstructionDisplay.CODEC.optionalFieldOf("display").forGetter(Instruction::display)
                                )
                                .apply(instance, Instruction::new)
    );

    @SuppressWarnings("unused")
    public static class Builder {
        @Nullable
        private Identifier parent = null;
        private final Map<String, Criterion<?>> criteria = new HashMap<>();
        private AdvancementRequirements requirements = AdvancementRequirements.EMPTY;
        @Nullable
        private InstructionDisplay display = null;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder parent(InstructionEntry parent) {
            return parent(parent.getId());
        }

        public Builder parent(Identifier parent) {
            this.parent = parent;
            return this;
        }

        public Builder requirements(AdvancementRequirements advancementRequirements) {
            this.requirements = advancementRequirements;
            return this;
        }

        public Builder criterion(String name, Criterion<?> criterion) {
            this.criteria.put(name, criterion);
            return this;
        }

        public Builder display(InstructionDisplay display) {
            this.display = display;
            return this;
        }

        public Builder display(ItemLike icon, Component title) {
            return display(icon, title, Component.empty());
        }

        public Builder display(ItemLike icon, Component title, Component description) {
            this.display = new InstructionDisplay(new ItemStackTemplate(icon.asItem()), title, description);
            return this;
        }

        public Builder requireCraft(ItemLike item) {
            return requireCraft(item, MinMaxBounds.Ints.atLeast(1));
        }

        public Builder requireCraft(ItemLike item, MinMaxBounds.Ints range) {
            PlayerPredicate playerPredicate = PlayerPredicate.Builder
                    .player()
                    .addStat(Stats.ITEM_CRAFTED, item.asItem().builtInRegistryHolder(), range)
                    .build();

            EntityPredicate entityPredicate = EntityPredicate.Builder.entity().player(playerPredicate).build();

            ContextAwarePredicate predicate = EntityPredicate.wrap(entityPredicate);

            PlayerTrigger.TriggerInstance triggerInstance = new PlayerTrigger.TriggerInstance(Optional.of(predicate));

            return criterion(id(item, "crafted"), CriteriaTriggers.TICK.createCriterion(triggerInstance));
        }

        public Builder requireItem(ItemLike item) {
            return criterion(id(item, "obtained"), InventoryChangeTrigger.TriggerInstance.hasItems(item));
        }

        private static String id(ItemLike item, String affix) {
            Holder.Reference<Item> reference = item.asItem().builtInRegistryHolder();
            ResourceKey<Item> key = reference.key();
            Identifier identifier = key.identifier();
            String name = identifier.getPath();
            return name + "_" + affix;
        }

        public InstructionEntry build(Identifier id) {
            if (this.requirements.isEmpty()) {
                this.requirements = AdvancementRequirements.allOf(this.criteria.keySet());
            }

            return new InstructionEntry(
                    id, new Instruction(
                    Optional.ofNullable(this.parent),
                    this.criteria,
                    this.requirements,
                    Optional.ofNullable(this.display)
            )
            );
        }

        @SuppressWarnings("UnusedReturnValue")
        public InstructionEntry build(Consumer<InstructionEntry> exporter, Identifier id) {
            InstructionEntry instructionEntry = this.build(id);
            exporter.accept(instructionEntry);
            return instructionEntry;
        }
    }
}
