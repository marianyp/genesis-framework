package dev.mariany.genesisframework.item;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.component.GFComponentTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.List;
import java.util.function.Function;

public class GFItems {
    public static final Item AGE_BOOK = register(
            "age_book",
            AgeBookItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .component(GFComponentTypes.AGES, List.of())
    );

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> itemKey = keyOf(name);
        Item item = factory.apply(properties.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }

    private static ResourceKey<Item> keyOf(String id) {
        return ResourceKey.create(Registries.ITEM, GenesisFramework.id(id));
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Items");
    }
}
