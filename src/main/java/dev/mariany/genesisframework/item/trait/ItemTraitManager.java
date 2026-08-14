package dev.mariany.genesisframework.item.trait;

import dev.mariany.genesisframework.event.server.entity.ServerEntityEvents;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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

public class ItemTraitManager {
    private final ItemAttributeModifiersSupplier itemAttributeModifiersSupplier;
    private final Supplier<List<ItemTrait>> itemTraitsSupplier;

    public ItemTraitManager(
            ItemAttributeModifiersSupplier itemAttributeModifiersSupplier,
            Supplier<List<ItemTrait>> itemTraitsSupplier
    ) {
        this.itemAttributeModifiersSupplier = itemAttributeModifiersSupplier;
        this.itemTraitsSupplier = itemTraitsSupplier;
    }

    public void bootstrap() {
        ServerEntityEvents.EQUIPMENT_CHANGED.register(this::onEquipmentChange);
    }

    public void onEquipmentChange(Player player) {
        Map<EquipmentSlot, ItemStack> items = new HashMap<>();

        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack stack = player.getItemBySlot(slot);
            items.put(slot, stack);
        }

        this.onEquipmentChange(player, items);
    }

    private void onEquipmentChange(LivingEntity livingEntity, Map<EquipmentSlot, ItemStack> changedItems) {
        if (!(livingEntity instanceof ServerPlayer serverPlayer)) {
            return;
        }

        this.updateAttributes(serverPlayer, changedItems);
    }

    private void updateAttributes(ServerPlayer serverPlayer, Map<EquipmentSlot, ItemStack> updatedItems) {
        if (updatedItems.isEmpty()) {
            return;
        }

        AttributeMap attributeMap = serverPlayer.getAttributes();

        resetAttributes(attributeMap, this.itemTraitsSupplier.get());

        for (Map.Entry<EquipmentSlot, ItemStack> entry : updatedItems.entrySet()) {
            EquipmentSlot slot = entry.getKey();
            ItemStack stack = entry.getValue();

            if (stack.isEmpty() || stack.isBroken()) {
                continue;
            }

            List<ItemAttributeModifiers> modifiers = this.itemAttributeModifiersSupplier.get(serverPlayer, stack);

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

        if (!present) {
            return;
        }

        instance.addTransientModifier(attributeModifier);
    }

    @FunctionalInterface
    public interface ItemAttributeModifiersSupplier {
        List<ItemAttributeModifiers> get(ServerPlayer serverPlayer, ItemStack stack);
    }
}
