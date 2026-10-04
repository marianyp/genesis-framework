package dev.mariany.genesisframework.registry;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.age.AgeDataLoader;
import dev.mariany.genesisframework.instruction.InstructionDataLoader;
import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.function.Function;

public final class GFDataResources {
    private GFDataResources() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Data Resources");
        registerBeforeVanilla("ages", AgeDataLoader::new);
        registerBeforeVanilla("instructions", InstructionDataLoader::new);
    }

    private static void registerBeforeVanilla(
            String name,
            Function<HolderLookup.Provider, PreparableReloadListener> factory
    ) {
        Identifier id = GenesisFramework.id(name);
        DataResourceLoader loader = DataResourceLoader.get();
        loader.registerReloadListener(id, factory);
        loader.addListenerOrdering(id, ResourceReloaderKeys.BEFORE_VANILLA);
    }
}
