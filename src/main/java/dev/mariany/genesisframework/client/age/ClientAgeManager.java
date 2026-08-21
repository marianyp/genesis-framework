package dev.mariany.genesisframework.client.age;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.age.AgeItemRestrictions;
import dev.mariany.genesisframework.age.AgeFormatter;
import dev.mariany.genesisframework.age.PartialAgeItemRestrictions;
import dev.mariany.genesisframework.client.age.requirement.AgeRequirementData;
import dev.mariany.genesisframework.event.client.item.ClientItemEvents;
import dev.mariany.genesisframework.item.trait.ItemTrait;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ClientAgeManager {
    private final Set<Runnable> itemRestrictionListeners = new HashSet<>();

    private AgeItemRestrictions itemRestrictions = AgeItemRestrictions.EMPTY;
    private boolean initiatedItemRestrictions = false;
    private Set<Item> lockedItems = Collections.emptySet();

    public void bootstrap() {
        GenesisFramework.bootstrapLog("Client Age Manager");
        ClientItemEvents.ADD_ATTRIBUTE_TOOLTIPS.register(this::addAttributeTooltips);
    }

    private void addAttributeTooltips(
            ItemStack stack,
            TooltipDisplay display,
            Player player,
            Consumer<Component> consumer
    ) {
        if (!display.shows(DataComponents.ATTRIBUTE_MODIFIERS)) {
            return;
        }

        addAttributeTooltips(stack, consumer);
    }

    private void addAttributeTooltips(ItemStack stack, Consumer<Component> consumer) {
        for (Map.Entry<Identifier, List<ItemTrait>> entry : this.itemRestrictions.traitsByAge().entrySet()) {
            Identifier ageId = entry.getKey();
            List<ItemTrait> traits = entry.getValue();
            ItemAttributeModifiers itemAttributeModifiers = aggregateItemAttributeModifiers(stack, traits);
            addAttributeTooltips(itemAttributeModifiers, ageId, consumer);
        }
    }

    private static ItemAttributeModifiers aggregateItemAttributeModifiers(ItemStack stack, List<ItemTrait> traits) {
        List<ItemAttributeModifiers.Entry> merged = traits
                .stream()
                .filter(itemTrait -> itemTrait.ingredient().test(stack))
                .flatMap(itemTrait -> itemTrait.attributeModifiers().modifiers().stream())
                .toList();

        return new ItemAttributeModifiers(merged);
    }

    private static void addAttributeTooltips(
            ItemAttributeModifiers itemAttributeModifiers,
            Identifier ageId,
            Consumer<Component> consumer
    ) {
        MutableBoolean first = new MutableBoolean(true);

        itemAttributeModifiers.modifiers().forEach(itemAttributeModifier -> {
            Holder<Attribute> attribute = itemAttributeModifier.attribute();
            AttributeModifier modifier = itemAttributeModifier.modifier();
            ItemAttributeModifiers.Display display = itemAttributeModifier.display();

            if (display == ItemAttributeModifiers.Display.hidden()) {
                return;
            }

            addAttributeHeader(ageId, consumer, first);

            display.apply(consumer, getPlayer(), attribute, modifier);
        });
    }

    private static void addAttributeHeader(
            Identifier ageId,
            Consumer<Component> consumer,
            MutableBoolean first
    ) {
        if (first.isFalse()) {
            return;
        }

        MutableComponent ageItemModifierComponent = Component.translatable(
                "item.modifiers.genesisframework.age",
                AgeFormatter.title(ageId)
        );

        consumer.accept(CommonComponents.EMPTY);
        consumer.accept(ageItemModifierComponent.withStyle(ChatFormatting.GRAY));
        first.setFalse();
    }

    private static Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    public void reset() {
        GenesisFramework.LOGGER.info("Resetting Age Manager");

        this.itemRestrictions = AgeItemRestrictions.EMPTY;
        this.initiatedItemRestrictions = false;
        this.lockedItems = Collections.emptySet();
        this.notifyItemRestrictionListeners();
    }

    public boolean isUnlocked(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        return !this.lockedItems.contains(stack.getItem());
    }

    public List<AgeRequirementData> getAgeRequirements() {
        return this.getGatedStacks()
                   .stream()
                   .map(this::getAgeRequirements)
                   .flatMap(Optional::stream)
                   .toList();
    }

    public Optional<AgeRequirementData> getAgeRequirements(ItemStack stack) {
        List<Identifier> requiredAgeIds = this.getRequiredAgeIds(stack);

        if (requiredAgeIds.isEmpty()) {
            return Optional.empty();
        }

        Set<Identifier> unlockedAgeIds = requiredAgeIds
                .stream()
                .filter(ageId -> this.isUnlockedForAge(ageId, stack))
                .collect(Collectors.toSet());

        return Optional.of(new AgeRequirementData(stack, requiredAgeIds, unlockedAgeIds));
    }

    private boolean isUnlockedForAge(Identifier ageId, ItemStack stack) {
        return !hasMatchingIngredient(
                this.itemRestrictions.lockedByAge().getOrDefault(ageId, List.of()),
                stack
        );
    }

    private List<Identifier> getRequiredAgeIds(ItemStack stack) {
        return this.itemRestrictions
                .gatedByAge()
                .entrySet()
                .stream()
                .filter(entry -> hasMatchingIngredient(entry.getValue(), stack))
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    private static boolean hasMatchingIngredient(List<Ingredient> ingredients, ItemStack stack) {
        return ingredients.stream().anyMatch(ingredient -> ingredient.test(stack));
    }

    private List<ItemStack> getGatedStacks() {
        return this.itemRestrictions
                .gatedByAge()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .flatMap(Ingredient::items)
                .map(Holder::value)
                .distinct()
                .map(Item::getDefaultInstance)
                .toList();
    }

    public void addItemRestrictionListener(Runnable listener) {
        this.itemRestrictionListeners.add(listener);
    }

    public void removeItemRestrictionListener(Runnable listener) {
        this.itemRestrictionListeners.remove(listener);
    }

    public void updateItemRestrictions(AgeItemRestrictions restrictions) {
        this.applyItemRestrictions(restrictions);
    }

    public void updateItemRestrictions(PartialAgeItemRestrictions restrictions) {
        this.applyItemRestrictions(restrictions.toAgeItemRestrictions(this.itemRestrictions));
    }

    private void applyItemRestrictions(AgeItemRestrictions updatedItemRestrictions) {
        if (updatedItemRestrictions.equals(this.itemRestrictions)) {
            return;
        }

        boolean initial = !this.initiatedItemRestrictions;

        List<Ingredient> oldLockedItems = flatten(this.itemRestrictions.lockedByAge());
        List<Ingredient> newLockedItems = flatten(updatedItemRestrictions.lockedByAge());

        int oldLockedItemsSize = oldLockedItems.size();
        int newLockedItemsSize = newLockedItems.size();

        List<Ingredient> lockedItemsDifference = getDifference(oldLockedItems, newLockedItems);

        this.itemRestrictions = updatedItemRestrictions;
        this.initiatedItemRestrictions = true;

        this.rebuildLockedItemsSet();

        this.notifyItemRestrictionListeners();

        GenesisFramework.LOGGER.info(
                "Updated item restrictions. Old locked items count: {} | New locked items count: {}",
                oldLockedItemsSize,
                newLockedItemsSize
        );

        if (initial) {
            return;
        }

        this.afterUpdateItemUnlocks(lockedItemsDifference);
    }

    private void rebuildLockedItemsSet() {
        this.lockedItems = this.itemRestrictions
                .lockedByAge().values().stream()
                .flatMap(Collection::stream)
                .flatMap(Ingredient::items)
                .map(Holder::value)
                .collect(Collectors.toSet());
    }

    private static List<Ingredient> flatten(Map<Identifier, List<Ingredient>> ingredientsByAge) {
        return ingredientsByAge.values().stream().flatMap(Collection::stream).toList();
    }

    private void notifyItemRestrictionListeners() {
        List.copyOf(this.itemRestrictionListeners).forEach(Runnable::run);
    }

    private static List<Ingredient> getDifference(Collection<Ingredient> before, Collection<Ingredient> after) {
        Set<Ingredient> ingredients = new HashSet<>(after);

        return before.stream()
                     .filter(oldIngredient -> !ingredients.contains(oldIngredient))
                     .toList();
    }

    private void afterUpdateItemUnlocks(Collection<Ingredient> changes) {
        Minecraft client = Minecraft.getInstance();
        ToastManager toastManager = client.gui.toastManager();

        for (Ingredient ingredient : changes) {
            ingredient.items()
                      .forEach(entry -> RecipeToast.addOrUpdate(toastManager, createRecipeDisplay(entry)));
        }
    }

    private static RecipeDisplay createRecipeDisplay(Holder<Item> entry) {
        return new RecipeDisplay() {
            @Override
            public SlotDisplay result() {
                return new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(entry.value()));
            }

            @Override
            public SlotDisplay craftingStation() {
                return SlotDisplay.Empty.INSTANCE;
            }

            @Nullable
            @Override
            public Type<? extends RecipeDisplay> type() {
                return null;
            }
        };
    }
}