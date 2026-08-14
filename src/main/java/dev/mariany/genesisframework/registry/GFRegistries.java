package dev.mariany.genesisframework.registry;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.age.Age;
import dev.mariany.genesisframework.instruction.Instruction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class GFRegistries {
    public static final ResourceKey<Registry<Age>> AGE = of("age");
    public static final ResourceKey<Registry<Instruction>> INSTRUCTION = of("instruction");

    private static <T> ResourceKey<Registry<T>> of(String path) {
        return ResourceKey.createRegistryKey(GenesisFramework.id(path));
    }

    private GFRegistries() {
    }
}
