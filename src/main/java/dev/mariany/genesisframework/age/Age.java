package dev.mariany.genesisframework.age;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mariany.genesisframework.advancement.criterion.CompleteTrialSpawnerTrigger;
import dev.mariany.genesisframework.stat.GFStats;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.DamageSourcePredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.predicates.entity.EntityEquipmentPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.entity.PlayerPredicate;
import net.minecraft.advancements.triggers.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public record Age(
        List<Ingredient> items,
        List<ResourceKey<Level>> dimensions,
        Optional<AgeItemTraits> itemTraits,
        Optional<Identifier> parent,
        boolean requiresParent,
        Map<String, Criterion<?>> criteria,
        AdvancementRequirements requirements,
        AgeDisplay display
) {
    private static final Codec<Map<String, Criterion<?>>> CRITERIA_CODEC =
            Codec.unboundedMap(Codec.STRING, Criterion.CODEC);

    public static final Codec<Age> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                                        Ingredient.CODEC
                                                .listOf()
                                                .optionalFieldOf("items", List.of())
                                                .forGetter(Age::items),
                                        ResourceKey
                                                .codec(Registries.DIMENSION)
                                                .listOf()
                                                .optionalFieldOf("dimensions", List.of())
                                                .forGetter(Age::dimensions),
                                        AgeItemTraits.CODEC
                                                .optionalFieldOf("item_traits")
                                                .forGetter(Age::itemTraits),
                                        Identifier.CODEC
                                                .optionalFieldOf("parent")
                                                .forGetter(Age::parent),
                                        Codec.BOOL
                                                .optionalFieldOf("requires_parent", true)
                                                .forGetter(Age::requiresParent),
                                        CRITERIA_CODEC
                                                .optionalFieldOf("criteria", new HashMap<>())
                                                .forGetter(Age::criteria),
                                        AdvancementRequirements.CODEC
                                                .optionalFieldOf("requirements", AdvancementRequirements.EMPTY)
                                                .forGetter(Age::requirements),
                                        AgeDisplay.CODEC.fieldOf("display").forGetter(Age::display)
                                )
                                .apply(instance, Age::new)
    );

    @SuppressWarnings("unused")
    public static class Builder {
        private final List<Ingredient> items = new ArrayList<>();
        private final List<ResourceKey<Level>> dimensions = new ArrayList<>();

        @Nullable
        private AgeItemTraits itemTraits = null;

        @Nullable
        private Identifier parent = null;

        private boolean requiresParent = true;

        private final Map<String, Criterion<?>> criteria = new HashMap<>();

        private AdvancementRequirements requirements = AdvancementRequirements.EMPTY;

        private AgeDisplay display;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder itemUnlock(Ingredient ingredient) {
            this.items.add(ingredient);
            return this;
        }

        public Builder itemUnlocks(List<Ingredient> ingredients) {
            this.items.addAll(ingredients);
            return this;
        }

        public Builder itemTraits(AgeItemTraits itemTraits) {
            this.itemTraits = itemTraits;
            return this;
        }

        public Builder dimensionUnlock(ResourceKey<Level> worldRegistryKey) {
            this.dimensions.add(worldRegistryKey);
            return this;
        }

        public Builder dimensionUnlocks(List<ResourceKey<Level>> worldRegistryKeys) {
            this.dimensions.addAll(worldRegistryKeys);
            return this;
        }

        public Builder parent(Identifier parent) {
            this.parent = parent;
            return this;
        }

        public Builder parentOptional() {
            this.requiresParent = false;
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

        public Builder requireAge(Identifier id) {
            Optional<String> categoryOpt = AgeEntry.getCategory(id);
            Optional<String> subpathOpt = AgeEntry.getSubPath(id);

            String name = subpathOpt
                    .map(
                            subpath -> categoryOpt
                                    .map(category -> "has_" + subpath + "_" + category + "_age")
                                    .orElse("has_" + subpath + "_age")
                    )
                    .orElse("has_age");

            Identifier advancementId = AgeEntry.getAdvancementId(id);

            PlayerPredicate playerPredicate = PlayerPredicate.Builder
                    .player()
                    .checkAdvancementDone(advancementId, true)
                    .build();

            EntityPredicate.Builder entityPredicate = EntityPredicate.Builder.entity().player(playerPredicate);

            ContextAwarePredicate wrappedEntityPredicate = EntityPredicate.wrap(entityPredicate);
            Optional<ContextAwarePredicate> optionalPlayerPredicate = Optional.of(wrappedEntityPredicate);

            PlayerTrigger.TriggerInstance triggerInstance = new PlayerTrigger.TriggerInstance(optionalPlayerPredicate);

            Criterion<PlayerTrigger.TriggerInstance> criterion = CriteriaTriggers.TICK.createCriterion(triggerInstance);

            return criterion(name, criterion);
        }

        public Builder requireKill(
                HolderLookup.RegistryLookup<EntityType<?>> entityLookup,
                EntityType<?> entityType,
                int atLeast
        ) {
            Holder.Reference<EntityType<?>> entityTypeHolder = entityType.builtInRegistryHolder();
            MinMaxBounds.Ints previousKillCount = MinMaxBounds.Ints.atLeast(atLeast - 1);

            PlayerPredicate playerPredicate = PlayerPredicate.Builder
                    .player()
                    .addStat(
                            Stats.ENTITY_KILLED,
                            entityTypeHolder,
                            previousKillCount
                    )
                    .build();

            EntityPredicate.Builder sourceEntityPredicate = EntityPredicate.Builder
                    .entity()
                    .player(playerPredicate);

            DamageSourcePredicate.Builder damageSourcePredicate = DamageSourcePredicate.Builder
                    .damageType()
                    .source(sourceEntityPredicate);

            EntityPredicate.Builder killedEntityPredicate = EntityPredicate.Builder
                    .entity()
                    .of(entityLookup, entityType);

            Criterion<KilledTrigger.TriggerInstance> trigger = KilledTrigger.TriggerInstance.playerKilledEntity(
                    killedEntityPredicate,
                    damageSourcePredicate
            );

            String entityTypePath = EntityType.getKey(entityType).getPath();
            String name = "killed_" + atLeast + entityTypePath;

            return criterion(name, trigger);
        }

        public Builder requireKillHostiles(int atLeast) {
            return criterion(
                    "killed_" + atLeast + "_hostiles",
                    CriteriaTriggers.TICK.createCriterion(
                            PlayerTrigger.TriggerInstance.located(
                                    EntityPredicate.Builder.entity().player(
                                            PlayerPredicate.Builder.player().addStat(
                                                    Stats.CUSTOM,
                                                    GFStats.HOSTILE_KILLS,
                                                    MinMaxBounds.Ints.atLeast(atLeast)
                                            ).build()
                                    )
                            ).triggerInstance()
                    )
            );
        }

        public Builder requireTrialWearing(
                HolderLookup.RegistryLookup<Item> itemLookup,
                boolean ominous,
                Item head,
                Item chest,
                Item legs,
                Item feet
        ) {
            List<String> requirements = new ArrayList<>();

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                EntityEquipmentPredicate.Builder builder = EntityEquipmentPredicate.Builder.equipment();

                @Nullable EntityEquipmentPredicate entityEquipmentPredicate = switch (slot) {
                    case HEAD -> builder.head(ItemPredicate.Builder.item().of(itemLookup, head)).build();
                    case CHEST -> builder.chest(ItemPredicate.Builder.item().of(itemLookup, chest)).build();
                    case LEGS -> builder.legs(ItemPredicate.Builder.item().of(itemLookup, legs)).build();
                    case FEET -> builder.feet(ItemPredicate.Builder.item().of(itemLookup, feet)).build();
                    default -> null;
                };

                if (entityEquipmentPredicate == null) {
                    continue;
                }

                String name = "trial_completed_with_" + slot.getSerializedName();

                criterion(
                        name,
                        CompleteTrialSpawnerTrigger.TriggerInstance.create(
                                EntityPredicate.wrap(
                                        EntityPredicate.Builder.entity().equipment(entityEquipmentPredicate).build()
                                ),
                                ominous
                        )
                );

                requirements.add(name);
            }

            this.requirements = AdvancementRequirements.anyOf(requirements);

            return this;
        }

        public Builder requireTimePlayed(int ticks) {
            ContextAwarePredicate predicate = EntityPredicate.wrap(
                    EntityPredicate.Builder.entity().player(
                            PlayerPredicate.Builder.player().addStat(
                                    Stats.CUSTOM,
                                    BuiltInRegistries.CUSTOM_STAT.getOrThrow(
                                            ResourceKey.create(Registries.CUSTOM_STAT, Stats.PLAY_TIME)
                                    ),
                                    MinMaxBounds.Ints.atLeast(ticks)
                            ).build()
                    ).build()
            );

            return criterion(
                    "time_played",
                    CriteriaTriggers.TICK.createCriterion(new PlayerTrigger.TriggerInstance(Optional.of(predicate)))
            );
        }

        public Age.Builder requireCraft(ItemLike item) {
            return requireCraft(item, MinMaxBounds.Ints.atLeast(1));
        }

        public Age.Builder requireCraft(ItemLike item, MinMaxBounds.Ints range) {
            PlayerPredicate playerPredicate = PlayerPredicate.Builder
                    .player()
                    .addStat(Stats.ITEM_CRAFTED, item.asItem().builtInRegistryHolder(), range)
                    .build();

            EntityPredicate entityPredicate = EntityPredicate.Builder.entity().player(playerPredicate).build();

            ContextAwarePredicate predicate = EntityPredicate.wrap(entityPredicate);

            PlayerTrigger.TriggerInstance triggerInstance = new PlayerTrigger.TriggerInstance(Optional.of(predicate));

            return criterion(id(item, "crafted"), CriteriaTriggers.TICK.createCriterion(triggerInstance));
        }

        public Age.Builder requireItem(ItemLike item) {
            return criterion(id(item, "obtained"), InventoryChangeTrigger.TriggerInstance.hasItems(item));
        }

        private static String id(ItemLike item, String affix) {
            Holder.Reference<Item> reference = item.asItem().builtInRegistryHolder();
            ResourceKey<Item> key = reference.key();
            Identifier identifier = key.identifier();
            String name = identifier.getPath();
            return name + "_" + affix;
        }

        public Builder display(AgeDisplay display) {
            this.display = display;
            return this;
        }

        public Builder display(ItemLike icon, Component title) {
            return this.display(icon, title, Component.empty());
        }

        public Builder display(ItemLike icon, Component title, Component description) {
            this.display = new AgeDisplay(new ItemStackTemplate(icon.asItem()), title, description);
            return this;
        }

        public AgeEntry build(Identifier id) {
            return new AgeEntry(
                    id,
                    new Age(
                            this.items,
                            this.dimensions,
                            Optional.ofNullable(this.itemTraits),
                            Optional.ofNullable(this.parent),
                            this.requiresParent,
                            this.criteria,
                            this.requirements,
                            this.display
                    )
            );
        }

        public AgeEntry build(Consumer<AgeEntry> exporter, Identifier id) {
            AgeEntry ageEntry = this.build(id);
            exporter.accept(ageEntry);
            return ageEntry;
        }
    }
}
