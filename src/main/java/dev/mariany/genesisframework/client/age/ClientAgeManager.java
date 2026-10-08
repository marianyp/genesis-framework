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
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ClientAgeManager {
    private final Set<Runnable> itemRestrictionListeners = new HashSet<>();

    @Nullable
    private List<ItemStack> gatedDisplayStacks = List.of();

    private IngredientIndex lockedIngredients = IngredientIndex.EMPTY;
    private IngredientIndex gatedIngredients = IngredientIndex.EMPTY;

    @Nullable
    private AgeItemRestrictions itemRestrictions;

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
        for (Map.Entry<Identifier, List<ItemTrait>> entry : this.getItemRestrictions().traitsByAge().entrySet()) {
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

        this.gatedDisplayStacks = List.of();
        this.gatedIngredients = IngredientIndex.EMPTY;
        this.lockedIngredients = IngredientIndex.EMPTY;
        this.itemRestrictions = null;

        this.notifyItemRestrictionListeners();
    }

    public boolean isUnlocked(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        return !this.lockedIngredients.matches(stack);
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

        Set<Identifier> lockedAgeIds = this.lockedIngredients.getMatchingAgeIds(stack);

        Set<Identifier> unlockedAgeIds = requiredAgeIds
                .stream()
                .filter(ageId -> !lockedAgeIds.contains(ageId))
                .collect(Collectors.toSet());

        return Optional.of(new AgeRequirementData(stack, requiredAgeIds, unlockedAgeIds));
    }

    private List<Identifier> getRequiredAgeIds(ItemStack stack) {
        return this.gatedIngredients.getMatchingAgeIds(stack).stream().sorted().toList();
    }

    private List<ItemStack> getGatedStacks() {
        if (this.gatedDisplayStacks == null) {
            Level level = Minecraft.getInstance().level;

            if (level == null) {
                return List.of();
            }

            ContextMap context = SlotDisplayContext.fromLevel(level);
            Set<ItemStack> stacks = ItemStackLinkedSet.createTypeAndComponentsSet();

            this.gatedIngredients.forEachIngredient(ingredient -> {
                for (ItemStack stack : ingredient.display().resolveForStacks(context)) {
                    if (ingredient.test(stack)) {
                        ItemStack copy = stack.copy();
                        copy.setCount(1);
                        stacks.add(copy);
                    }
                }
            });

            this.gatedDisplayStacks = List.copyOf(stacks);
        }

        return this.gatedDisplayStacks;
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
        this.applyItemRestrictions(restrictions.toAgeItemRestrictions(this.getItemRestrictions()));
    }

    private void applyItemRestrictions(AgeItemRestrictions updatedItemRestrictions) {
        boolean initial = this.itemRestrictions == null;
        AgeItemRestrictions previousItemRestrictions = this.getItemRestrictions();

        if (!initial && updatedItemRestrictions.equals(previousItemRestrictions)) {
            return;
        }

        List<Ingredient> oldLockedItems = flatten(previousItemRestrictions.lockedByAge());
        List<Ingredient> newLockedItems = flatten(updatedItemRestrictions.lockedByAge());

        int oldLockedItemsSize = oldLockedItems.size();
        int newLockedItemsSize = newLockedItems.size();

        List<Ingredient> lockedItemsDifference = getDifference(oldLockedItems, newLockedItems);

        this.updateIngredientIndexes(previousItemRestrictions, updatedItemRestrictions);
        this.itemRestrictions = updatedItemRestrictions;

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

    private void updateIngredientIndexes(
            AgeItemRestrictions previousItemRestrictions,
            AgeItemRestrictions updatedItemRestrictions
    ) {
        if (!updatedItemRestrictions.gatedByAge().equals(previousItemRestrictions.gatedByAge())) {
            this.gatedIngredients = IngredientIndex.create(updatedItemRestrictions.gatedByAge());
            this.gatedDisplayStacks = null;
        }

        if (!updatedItemRestrictions.lockedByAge().equals(previousItemRestrictions.lockedByAge())) {
            this.lockedIngredients = IngredientIndex.create(updatedItemRestrictions.lockedByAge());
        }
    }

    private AgeItemRestrictions getItemRestrictions() {
        return Objects.requireNonNullElse(this.itemRestrictions, AgeItemRestrictions.EMPTY);
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
            RecipeToast.addOrUpdate(toastManager, createRecipeDisplay(ingredient));
        }
    }

    private static RecipeDisplay createRecipeDisplay(Ingredient ingredient) {
        return new RecipeDisplay() {
            @Override
            public SlotDisplay result() {
                return ingredient.display();
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