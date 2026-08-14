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
    private static final Identifier AGES = GenesisFramework.id("ages");
    private static final Identifier INSTRUCTIONS = GenesisFramework.id("instructions");

    private GFDataResources() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Data Resources");
        registerBeforeAdvancements(AGES, AgeDataLoader::new);
        registerBeforeAdvancements(INSTRUCTIONS, InstructionDataLoader::new);
    }

    private static void registerBeforeAdvancements(
            Identifier id,
            Function<HolderLookup.Provider, PreparableReloadListener> factory
    ) {
        DataResourceLoader loader = DataResourceLoader.get();
        loader.registerReloadListener(id, factory);
        loader.addListenerOrdering(id, ResourceReloaderKeys.Server.ADVANCEMENTS);
    }
}
