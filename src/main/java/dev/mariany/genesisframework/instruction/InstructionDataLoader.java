package dev.mariany.genesisframework.instruction;

import com.google.common.collect.ImmutableMap;
import dev.mariany.genesisframework.registry.GFRegistryKeys;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class InstructionDataLoader extends SimpleJsonResourceReloadListener<Instruction> {
    public InstructionDataLoader(HolderLookup.Provider registries) {
        super(registries, Instruction.CODEC, GFRegistryKeys.INSTRUCTION);
    }

    @Override
    protected void apply(Map<Identifier, Instruction> map, ResourceManager manager, ProfilerFiller profiler) {
        ServerInstructionManager serverInstructionManager = ServerInstructionManager.getInstance();
        serverInstructionManager.clear();

        ImmutableMap.Builder<Identifier, InstructionEntry> builder = ImmutableMap.builder();

        map.forEach((id, instruction) -> builder.put(id, new InstructionEntry(id, instruction)));

        builder.buildOrThrow().values().forEach(serverInstructionManager::add);
    }
}
