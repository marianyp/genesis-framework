package dev.mariany.genesisframework.age;

import dev.mariany.genesisframework.item.ItemTrait;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.stream.Stream;

public class ServerAgeManager {
    private static final ServerAgeManager INSTANCE = new ServerAgeManager();

    private final Map<Identifier, AgeEntry> ages = new Object2ObjectOpenHashMap<>();

    public static ServerAgeManager getInstance() {
        return INSTANCE;
    }

    public void onEquipmentUpdate(ServerPlayer player) {
        AgeAttributeManager.onEquipmentUpdate(
                player,
                stack -> this.getItemAttributeModifiers(player, stack),
                this::getAllItemTraits
        );
    }

    public void onEquipmentUpdate(ServerPlayer player, Map<EquipmentSlot, ItemStack> changedItems) {
        AgeAttributeManager.onEquipmentUpdate(
                player,
                changedItems,
                stack -> this.getItemAttributeModifiers(player, stack),
                this::getAllItemTraits
        );
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

    public boolean isUnlocked(ServerPlayer player, ResourceKey<Level> worldRegistryKey) {
        return allUnlocked(player, getRequiredAges(worldRegistryKey));
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

            if (parentId.isPresent()) {
                AgeEntry parentEntry = this.ages.get(parentId.get());

                if (parentEntry != null) {
                    ageEntry = parentEntry;
                    continue;
                }
            }

            return true;
        }

        return false;
    }

    public List<AgeEntry> getRequiredAges(ItemStack stack) {
        List<AgeEntry> requiredAges = new ArrayList<>();

        for (AgeEntry placedAge : this.getAges()) {
            List<Ingredient> itemUnlocks = placedAge.getAge().items();

            for (Ingredient ingredient : itemUnlocks) {
                if (ingredient.test(stack)) {
                    requiredAges.add(placedAge);
                    break;
                }
            }
        }

        return requiredAges;
    }

    public List<AgeEntry> getRequiredAges(ResourceKey<Level> worldRegistryKey) {
        List<AgeEntry> requiredAges = new ArrayList<>();

        for (AgeEntry placedAge : this.getAges()) {
            List<ResourceKey<Level>> dimensions = placedAge.getAge().dimensions();

            for (ResourceKey<Level> dimension : dimensions) {
                if (worldRegistryKey.identifier().equals(dimension.identifier())) {
                    requiredAges.add(placedAge);
                    break;
                }
            }
        }

        return requiredAges;
    }

    public Optional<AgeEntry> find(AdvancementHolder advancementEntry) {
        return this.getAges()
                   .stream()
                   .filter(ageEntry -> ageEntry.getAdvancementHolder().id().equals(advancementEntry.id()))
                   .findAny();
    }

    public Optional<AgeEntry> find(Age age) {
        return this.getAges().stream().filter(ageEntry -> ageEntry.getAge().equals(age)).findAny();
    }

    public Optional<AgeEntry> get(Identifier id) {
        return Optional.ofNullable(this.ages.get(id));
    }

    public Collection<AgeEntry> getAges() {
        return this.ages.values();
    }

    public List<Ingredient> getLockedItems(ServerPlayer player) {
        return getPlayerAges(player, false)
                .stream()
                .flatMap(ageEntry -> ageEntry.getAge().items().stream())
                .toList();
    }

    public List<ItemAttributeModifiers> getItemAttributeModifiers(ServerPlayer player, ItemStack stack) {
        return this.getActiveTraits(player)
                   .values()
                   .stream()
                   .flatMap(Collection::stream)
                   .filter(itemTrait -> itemTrait.ingredient().test(stack))
                   .map(ItemTrait::attributeModifiers)
                   .toList();
    }

    public Map<AgeEntry, List<ItemTrait>> getActiveTraits(ServerPlayer player) {
        Map<AgeEntry, List<ItemTrait>> activeTraits = new HashMap<>();

        for (AgeEntry age : this.getAges()) {
            List<ItemTrait> itemTraits = collectItemTraits(age, player);

            if (!itemTraits.isEmpty()) {
                activeTraits.put(age, itemTraits);
            }
        }

        return activeTraits;
    }

    public List<ItemTrait> getAllItemTraits() {
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

    private static List<ItemTrait> collectItemTraits(AgeEntry ageEntry, ServerPlayer player) {
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
