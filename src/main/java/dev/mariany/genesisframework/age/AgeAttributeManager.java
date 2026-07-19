package dev.mariany.genesisframework.age;

import dev.mariany.genesisframework.item.ItemTrait;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class AgeAttributeManager {
    private AgeAttributeManager() {
    }

    public static void onEquipmentUpdate(
            Player player,
            ItemAttributeModifiersSupplier itemAttributeModifiersSupplier,
            Supplier<List<ItemTrait>> itemTraitsSupplier
    ) {
        Map<EquipmentSlot, ItemStack> items = new HashMap<>();

        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack stack = player.getItemBySlot(slot);
            items.put(slot, stack);
        }

        onEquipmentUpdate(player, items, itemAttributeModifiersSupplier, itemTraitsSupplier);
    }

    public static void onEquipmentUpdate(
            Player player,
            Map<EquipmentSlot, ItemStack> updatedItems,
            ItemAttributeModifiersSupplier itemAttributeModifiersSupplier,
            Supplier<List<ItemTrait>> itemTraitsSupplier
    ) {
        if (updatedItems.isEmpty()) {
            return;
        }

        AttributeMap attributeMap = player.getAttributes();

        resetAttributes(attributeMap, itemTraitsSupplier.get());

        for (Map.Entry<EquipmentSlot, ItemStack> entry : updatedItems.entrySet()) {
            EquipmentSlot slot = entry.getKey();
            ItemStack stack = entry.getValue();

            if (stack.isEmpty() || stack.isBroken()) {
                continue;
            }

            List<ItemAttributeModifiers> modifiers = itemAttributeModifiersSupplier.get(stack);

            modifiers.forEach(itemAttributeModifiers -> updateAttributeModifier(
                    slot,
                    attributeMap,
                    itemAttributeModifiers,
                    true
            ));
        }
    }

    private static void resetAttributes(AttributeMap attributeMap, List<ItemTrait> itemTraits) {
        List<ItemAttributeModifiers> modifiers = itemTraits.stream().map(ItemTrait::attributeModifiers).toList();

        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            modifiers.forEach(itemAttributeModifiers -> updateAttributeModifier(
                    slot,
                    attributeMap,
                    itemAttributeModifiers,
                    false
            ));
        }
    }

    private static void updateAttributeModifier(
            EquipmentSlot slot,
            AttributeMap attributeMap,
            ItemAttributeModifiers itemAttributeModifiers,
            boolean present
    ) {
        itemAttributeModifiers.forEach(
                slot,
                (attribute, modifier) -> updateAttributeModifier(
                        attributeMap,
                        attribute,
                        modifier,
                        present
                )
        );
    }

    private static void updateAttributeModifier(
            AttributeMap attributeMap,
            Holder<Attribute> attribute,
            AttributeModifier attributeModifier,
            boolean present
    ) {
        AttributeInstance instance = attributeMap.getInstance(attribute);

        if (instance == null) {
            return;
        }

        instance.removeModifier(attributeModifier.id());

        if (present) {
            instance.addTransientModifier(attributeModifier);
        }
    }

    @FunctionalInterface
    public interface ItemAttributeModifiersSupplier {
        List<ItemAttributeModifiers> get(ItemStack stack);
    }
}
