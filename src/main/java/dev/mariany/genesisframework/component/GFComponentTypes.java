package dev.mariany.genesisframework.component;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.age.Age;
import dev.mariany.genesisframework.registry.GFRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.function.UnaryOperator;

public final class GFComponentTypes {
    public static final DataComponentType<List<ResourceKey<Age>>> AGES = register(
            "ages",
            builder -> builder.persistent(ResourceKey.codec(GFRegistries.AGE).listOf()
            ).cacheEncoding()
    );

    private GFComponentTypes() {
    }

    private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                GenesisFramework.id(id), builderOperator.apply(DataComponentType.builder()).build()
        );
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Component Types");
    }
}
