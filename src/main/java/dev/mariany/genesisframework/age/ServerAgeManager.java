package dev.mariany.genesisframework.age;

import dev.mariany.genesisframework.advancement.DynamicAdvancementManager;
import dev.mariany.genesisframework.event.server.age.ServerAgeEvents;
import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import dev.mariany.genesisframework.item.GFItems;
import dev.mariany.genesisframework.item.trait.ItemTrait;
import dev.mariany.genesisframework.item.trait.ItemTraitManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.advancements.*;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerAgeManager extends DynamicAdvancementManager {
    private final Map<Identifier, AgeEntry> ages = new Object2ObjectOpenHashMap<>();

    private final ItemTraitManager itemTraitManager = new ItemTraitManager(
            this::getItemAttributeModifiers,
            this::getAllItemTraits
    );

    @Override
    public void bootstrap() {
        super.bootstrap();

        this.itemTraitManager.bootstrap();

        ServerAgeEvents.SYNC.register(this::onAgeSync);
        ServerAdvancementEvents.ALLOW_AWARD.register(this::shouldAllowAward);
    }

    public void onAgeSync(ServerPlayer serverPlayer) {
        this.itemTraitManager.onEquipmentChange(serverPlayer);
    }

    private boolean shouldAllowAward(ServerPlayer player, AdvancementHolder advancement) {
        Optional<AgeEntry> optionalAgeEntry = this.find(advancement);

        if (optionalAgeEntry.isEmpty()) {
            return true;
        }

        Age age = optionalAgeEntry.get().getAge();
        Optional<Identifier> optionalParentId = age.parent();

        if (!age.requiresParent() || optionalParentId.isEmpty()) {
            return true;
        }

        while (optionalParentId.isPresent()) {
            Optional<AgeEntry> optionalParent = this.get(optionalParentId.get());

            if (optionalParent.isEmpty()) {
                break;
            }

            AgeEntry parentAgeEntry = optionalParent.get();
            Age parentAge = parentAgeEntry.getAge();

            if (parentAge.requiresParent()) {
                return parentAgeEntry.isDone(player);
            }

            optionalParentId = parentAge.parent();
        }

        return true;
    }

    @Override
    protected AdvancementHolder createRootAdvancement() {
        return new AdvancementHolder(
                AgeEntry.ROOT_ADVANCEMENT_ID,
                new Advancement(
                        Optional.empty(),
                        Optional.of(
                                new DisplayInfo(
                                        new ItemStackTemplate(GFItems.AGE_BOOK),
                                        Component.translatable("advancements.genesisframework.ages.title"),
                                        Component.empty(),
                                        Optional.of(
                                                new ClientAsset.ResourceTexture(
                                                        Identifier.withDefaultNamespace("block/dark_oak_planks")
                                                )
                                        ),
                                        AdvancementType.TASK,
                                        false,
                                        false,
                                        false
                                )
                        ),
                        AdvancementRewards.EMPTY,
                        Map.of(
                                "root",
                                new Criterion<>(
                                        CriteriaTriggers.TICK,
                                        PlayerTrigger.TriggerInstance.tick().triggerInstance()
                                )
                        ),
                        AdvancementRequirements.allOf(List.of("root")),
                        false
                )
        );
    }

    @Override
    protected List<AdvancementHolder> getAdvancements() {
        return this.getAges().stream().map(AgeEntry::getAdvancementHolder).toList();
    }

    public boolean isAgeGuarded(ItemLike item) {
        return this.isAgeGuarded(item.asItem().getDefaultInstance());
    }

    public boolean isAgeGuarded(ItemStack stack) {
        return this.getAges()
                   .stream()
                   .anyMatch(
                           ageEntry -> ageEntry
                                   .getAge()
                                   .items()
                                   .stream()
                                   .anyMatch(ingredient -> ingredient.test(stack))
                   );
    }

    public boolean isUnlocked(ServerPlayer player, ResourceKey<Level> levelResourceKey) {
        return allUnlocked(player, getRequiredAges(levelResourceKey));
    }

    public boolean isUnlocked(ServerPlayer player, Block block) {
        return isUnlocked(player, block.asItem().getDefaultInstance());
    }

    public boolean isUnlocked(ServerPlayer player, ItemStack stack) {
        return allUnlocked(player, getRequiredAges(stack));
    }

    public boolean allUnlocked(ServerPlayer player, Collection<AgeEntry> ages) {
        if (player.isCreative()) {
            return true;
        }

        if (ages.isEmpty()) {
            return true;
        }

        return ages.stream().allMatch(placedAge -> isDoneRecursively(placedAge, player));
    }

    public boolean isDoneRecursively(AgeEntry ageEntry, ServerPlayer player) {
        if (!ageEntry.getAge().requiresParent()) {
            return ageEntry.isDone(player);
        }

        while (ageEntry.isDone(player)) {
            Optional<Identifier> parentId = ageEntry.getAge().parent();

            if (parentId.isEmpty()) {
                return true;
            }

            AgeEntry parentEntry = this.ages.get(parentId.get());

            if (parentEntry == null) {
                return true;
            }

            ageEntry = parentEntry;
        }

        return false;
    }

    public List<AgeEntry> getRequiredAges(ItemStack stack) {
        return this.getAges()
                   .stream()
                   .filter(age -> age.getAge().items().stream().anyMatch(ingredient -> ingredient.test(stack)))
                   .toList();
    }

    public List<AgeEntry> getRequiredAges(ResourceKey<Level> levelResourceKey) {
        return this.getAges()
                   .stream()
                   .filter(age -> age
                           .getAge()
                           .dimensions()
                           .stream()
                           .anyMatch(
                                   dimension -> levelResourceKey
                                           .identifier()
                                           .equals(dimension.identifier())
                           )
                   )
                   .toList();
    }

    public Optional<AgeEntry> find(AdvancementHolder advancementEntry) {
        return this.getAges()
                   .stream()
                   .filter(ageEntry -> ageEntry.getAdvancementHolder().id().equals(advancementEntry.id()))
                   .findAny();
    }

    public Optional<AgeEntry> get(Identifier id) {
        return Optional.ofNullable(this.ages.get(id));
    }

    public Collection<AgeEntry> getAges() {
        return this.ages.values();
    }

    public Map<AgeEntry, List<Ingredient>> getLockedItemsByAge(ServerPlayer player) {
        return this.getPlayerAges(player, false)
                   .stream()
                   .collect(Collectors.toMap(ageEntry -> ageEntry, ageEntry -> ageEntry.getAge().items()));
    }

    public Map<AgeEntry, List<Ingredient>> getGatedItemsByAge() {
        return this.getAges()
                   .stream()
                   .collect(Collectors.toMap(Function.identity(), AgeEntry::getItems));
    }

    private List<ItemAttributeModifiers> getItemAttributeModifiers(ServerPlayer serverPlayer, ItemStack stack) {
        return this.getActiveTraits(serverPlayer)
                   .values()
                   .stream()
                   .flatMap(Collection::stream)
                   .filter(itemTrait -> itemTrait.ingredient().test(stack))
                   .map(ItemTrait::attributeModifiers)
                   .toList();
    }

    public Map<AgeEntry, List<ItemTrait>> getActiveTraits(ServerPlayer serverPlayer) {
        Map<AgeEntry, List<ItemTrait>> activeTraits = new HashMap<>();

        for (AgeEntry age : this.getAges()) {
            List<ItemTrait> itemTraits = collectItemTraits(serverPlayer, age);

            if (itemTraits.isEmpty()) {
                continue;
            }

            activeTraits.put(age, itemTraits);
        }

        return activeTraits;
    }

    private List<ItemTrait> getAllItemTraits() {
        return this.getAges()
                   .stream()
                   .map(ageEntry -> ageEntry.getAge().itemTraits())
                   .flatMap(Optional::stream)
                   .flatMap(ServerAgeManager::getItemTraits)
                   .toList();
    }

    private static Stream<ItemTrait> getItemTraits(AgeItemTraits ageItemTraits) {
        return Optional.ofNullable(ageItemTraits).stream().flatMap(ServerAgeManager::concatItemTraits);
    }

    private static Stream<ItemTrait> concatItemTraits(AgeItemTraits ageItemTraits) {
        return Stream.concat(ageItemTraits.beforeDone().stream(), ageItemTraits.afterDone().stream());
    }

    private static List<ItemTrait> collectItemTraits(ServerPlayer player, AgeEntry ageEntry) {
        Age age = ageEntry.getAge();
        boolean done = ageEntry.isDone(player);

        return age.itemTraits()
                  .map(ageItemTraits -> done ? ageItemTraits.afterDone() : ageItemTraits.beforeDone())
                  .orElse(Collections.emptyList());
    }

    private List<AgeEntry> getPlayerAges(ServerPlayer player, boolean done) {
        return getAges()
                .stream()
                .filter(ageEntry -> ageEntry.isDone(player) == done)
                .toList();
    }

    protected void add(AgeEntry age) {
        this.ages.put(age.getId(), age);
    }

    protected void clear() {
        this.ages.clear();
    }
}
