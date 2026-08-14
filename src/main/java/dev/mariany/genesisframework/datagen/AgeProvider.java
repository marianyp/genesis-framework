package dev.mariany.genesisframework.datagen;


import dev.mariany.genesisframework.age.Age;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.registry.GFRegistries;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AgeProvider extends FabricCodecDataProvider<Age> {
    public AgeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture, GFRegistries.AGE, Age.CODEC);
    }

    @Override
    protected void configure(BiConsumer<Identifier, Age> provider, HolderLookup.Provider registryLookup) {
        this.generateAges(registryLookup, (ageEntry) -> provider.accept(ageEntry.getId(), ageEntry.getAge()));
    }

    public abstract void generateAges(HolderLookup.Provider registryLookup, Consumer<AgeEntry> consumer);
}