package dev.mariany.genesisframework.registry;

import dev.mariany.genesisframework.age.Age;
import dev.mariany.genesisframework.instruction.Instruction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

public class GFRegistryKeys {
    public static final ResourceKey<Registry<Age>> AGE = ofVanilla("age");
    public static final ResourceKey<Registry<Instruction>> INSTRUCTION = ofVanilla("instruction");

    private static <T> ResourceKey<Registry<T>> ofVanilla(String id) {
        return ResourceKey.createRegistryKey(Identifier.withDefaultNamespace(id));
    }
}
