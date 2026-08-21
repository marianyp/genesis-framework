package dev.mariany.genesisframework.age;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public final class AgeFormatter {
    private AgeFormatter() {
    }

    public static Component title(Identifier id) {
        return Component.translatable(
                "age." + id.getNamespace() + "." + id.getPath().replace('/', '.')
        );
    }

    public static Component format(Identifier id) {
        Component title = title(id);
        Component age = Component.translatable("age.genesisframework.age");

        return category(id)
                .<Component>map(category -> Component.translatable(
                        "age.genesisframework.name.categorized",
                        category,
                        title,
                        age
                ))
                .orElseGet(() -> Component.translatable(
                        "age.genesisframework.name",
                        title,
                        age
                ));
    }

    public static Optional<Component> category(Identifier id) {
        String path = id.getPath();
        int separator = path.indexOf('/');

        if (separator < 0) {
            return Optional.empty();
        }

        String category = path.substring(0, separator);

        return Optional.of(Component.translatable("age." + id.getNamespace() + ".category." + category));
    }

}