package dev.mariany.genesisframework.datagen;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.mariany.genesisframework.age.Age;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.registry.GFRegistryKeys;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class AgeProvider implements DataProvider {
    protected final FabricPackOutput output;
    private final PackOutput.PathProvider pathResolver;
    private final CompletableFuture<HolderLookup.Provider> registryLookup;

    public AgeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        this.output = output;
        this.pathResolver = output.createRegistryElementsPathProvider(GFRegistryKeys.AGE);
        this.registryLookup = registryLookup;
    }

    public abstract void generateAges(HolderLookup.Provider registryLookup, Consumer<AgeEntry> consumer);

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return this.registryLookup.thenCompose(lookup -> {
            final Set<Identifier> identifiers = Sets.newHashSet();
            final Set<AgeEntry> ages = Sets.newHashSet();

            generateAges(lookup, ages::add);

            RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
            final List<CompletableFuture<?>> futures = new ArrayList<>();

            for (AgeEntry ageEntry : ages) {
                Identifier id = ageEntry.getId();

                if (!identifiers.add(id)) {
                    throw new IllegalStateException("Duplicate age " + ageEntry.getId());
                }

                JsonObject advancementJson = Age.CODEC.encodeStart(ops, ageEntry.getAge())
                        .getOrThrow(IllegalStateException::new).getAsJsonObject();

                FabricDataGenHelper.addConditions(advancementJson, FabricDataGenHelper.consumeConditions(ageEntry));

                futures.add(DataProvider.saveStable(writer, advancementJson, getOutputPath(ageEntry)));
            }

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    private Path getOutputPath(AgeEntry age) {
        return pathResolver.json(age.getId());
    }
}
