package dev.mariany.genesisframework.client.age;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.item.ItemTrait;
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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.*;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ClientAgeManager {
    private static final ClientAgeManager INSTANCE = new ClientAgeManager();

    private final List<Ingredient> lockedItems = new ArrayList<>();
    private final Map<String, List<ItemTrait>> traitsByLanguageKey = new HashMap<>();

    private boolean initiatedLockedItems = false;

    private ClientAgeManager() {
    }

    public static ClientAgeManager getInstance() {
        return INSTANCE;
    }

    public void addAttributeTooltips(ItemStack stack, TooltipDisplay display, Consumer<Component> consumer) {
        if (!display.shows(DataComponents.ATTRIBUTE_MODIFIERS)) {
            return;
        }

        addAttributeTooltips(stack, consumer);
    }

    private void addAttributeTooltips(ItemStack stack, Consumer<Component> consumer) {
        for (Map.Entry<String, List<ItemTrait>> entry : this.traitsByLanguageKey.entrySet()) {
            String languageKey = entry.getKey();
            List<ItemTrait> traits = entry.getValue();
            ItemAttributeModifiers itemAttributeModifiers = aggregateItemAttributeModifiers(stack, traits);
            addAttributeTooltips(itemAttributeModifiers, languageKey, consumer);
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
            String translationKey,
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

            if (first.isTrue()) {
                MutableComponent ageItemModifierComponent = Component.translatable(
                        "item.modifiers.genesisframework.age",
                        Component.translatable(translationKey)
                );

                consumer.accept(CommonComponents.EMPTY);
                consumer.accept(ageItemModifierComponent.withStyle(ChatFormatting.GRAY));

                first.setFalse();
            }

            display.apply(consumer, getPlayer(), attribute, modifier);
        });
    }

    private static Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    public void reset() {
        GenesisFramework.LOGGER.info("Resetting Client Age Manager");

        this.lockedItems.clear();
        this.traitsByLanguageKey.clear();
        this.initiatedLockedItems = false;
    }

    public boolean isUnlocked(ItemStack stack) {
        return this.lockedItems.stream().noneMatch(ingredient -> ingredient.test(stack));
    }

    public void updateTraits(Map<String, List<ItemTrait>> traitsByLanguageKey) {
        this.traitsByLanguageKey.clear();
        this.traitsByLanguageKey.putAll(traitsByLanguageKey);
    }

    public void updateLockedItems(Collection<Ingredient> changes) {
        boolean initial = !this.initiatedLockedItems;
        int oldSize = this.lockedItems.size();

        List<Ingredient> difference = getDifference(this.lockedItems, changes);

        this.lockedItems.clear();
        this.lockedItems.addAll(changes);
        this.initiatedLockedItems = true;

        GenesisFramework.LOGGER.info(
                "Updated age instructions. Old Size: {} | New Size: {}",
                oldSize,
                this.lockedItems.size()
        );

        if (!initial) {
            afterUpdateItemUnlocks(difference);
        }
    }

    private static List<Ingredient> getDifference(
            Collection<Ingredient> before,
            Collection<Ingredient> after
    ) {
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
                return new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.AIR));
            }

            @Override
            public Type<? extends RecipeDisplay> type() {
                return null;
            }
        };
    }
}
