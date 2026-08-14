package dev.mariany.genesisframework.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.mariany.genesisframework.GenesisFramework;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.StrictJsonParser;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class CodecDataResourceReloadListener<T>
        extends SimpleReloadListener<Map<Identifier, T>> {
    private final HolderLookup.Provider registries;
    private final Codec<T> codec;
    private final FileToIdConverter lister;
    private final String resourceType;

    protected CodecDataResourceReloadListener(
            HolderLookup.Provider registries,
            Codec<T> codec,
            ResourceKey<? extends Registry<T>> registryKey
    ) {
        this.registries = registries;
        this.codec = codec;
        this.lister = FileToIdConverter.registry(registryKey);
        this.resourceType = registryKey.identifier().getPath();
    }

    @Override
    protected final Map<Identifier, T> prepare(PreparableReloadListener.SharedState state) {
        RegistryOps<JsonElement> ops = this.registries.createSerializationContext(JsonOps.INSTANCE);
        Map<Identifier, T> result = new HashMap<>();

        Map<Identifier, Resource> resources = this.lister.listMatchingResources(state.resourceManager());

        for (Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier location = entry.getKey();
            Identifier id = this.lister.fileToId(location);

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement json = StrictJsonParser.parse(reader);

                if (json.isJsonObject() && json.getAsJsonObject().isEmpty()) {
                    continue;
                }

                DataResult<T> parsed = this.codec.parse(ops, json);

                parsed.ifSuccess(value -> {
                    if (result.putIfAbsent(id, value) == null) {
                        return;
                    }

                    throw new IllegalStateException(
                            "Duplicate " + this.resourceType + " data file ignored with ID " + id
                    );
                }).ifError(error -> GenesisFramework.LOGGER.error(
                        "Couldn't parse {} data file '{}' from '{}': {}",
                        this.resourceType,
                        id,
                        location,
                        error
                ));
            } catch (JsonParseException | IllegalArgumentException | IOException error) {
                GenesisFramework.LOGGER.error(
                        "Couldn't parse {} data file '{}' from '{}'",
                        this.resourceType,
                        id,
                        location,
                        error
                );
            }
        }

        return result;
    }
}
