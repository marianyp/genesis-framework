package dev.mariany.genesisframework.age;

import com.google.common.collect.ImmutableMap;
import dev.mariany.genesisframework.registry.GFRegistryKeys;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class AgeDataLoader extends SimpleJsonResourceReloadListener<Age> {
    public AgeDataLoader(HolderLookup.Provider registries) {
        super(registries, Age.CODEC, GFRegistryKeys.AGE);
    }

    @Override
    protected void apply(Map<Identifier, Age> map, ResourceManager manager, ProfilerFiller profiler) {
        ServerAgeManager serverAgeManager = ServerAgeManager.getInstance();
        serverAgeManager.clear();

        ImmutableMap.Builder<Identifier, AgeEntry> builder = ImmutableMap.builder();

        map.forEach((id, age) -> builder.put(id, new AgeEntry(id, age)));

        builder.buildOrThrow().values().forEach(serverAgeManager::add);
    }
}
