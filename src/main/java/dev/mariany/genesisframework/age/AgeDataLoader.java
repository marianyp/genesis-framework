package dev.mariany.genesisframework.age;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.registry.GFRegistries;
import dev.mariany.genesisframework.resource.CodecDataResourceReloadListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Map;

public class AgeDataLoader extends CodecDataResourceReloadListener<Age> {
    public AgeDataLoader(HolderLookup.Provider registries) {
        super(registries, Age.CODEC, GFRegistries.AGE);
    }

    @Override
    protected void apply(Map<Identifier, Age> prepared, PreparableReloadListener.SharedState state) {
        ServerAgeManager serverAgeManager = GenesisFramework.getServerAgeManager();
        serverAgeManager.clear();
        prepared.forEach((id, age) -> serverAgeManager.add(new AgeEntry(id, age)));
    }
}
