package dev.mariany.genesisframework.instruction;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.registry.GFRegistries;
import dev.mariany.genesisframework.resource.CodecDataResourceReloadListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Map;

public class InstructionDataLoader extends CodecDataResourceReloadListener<Instruction> {
    public InstructionDataLoader(HolderLookup.Provider registries) {
        super(registries, Instruction.CODEC, GFRegistries.INSTRUCTION);
    }

    @Override
    protected void apply(Map<Identifier, Instruction> prepared, PreparableReloadListener.SharedState state) {
        ServerInstructionManager serverInstructionManager = GenesisFramework.getServerInstructionManager();
        serverInstructionManager.clear();
        prepared.forEach((id, instruction) -> serverInstructionManager.add(
                new InstructionEntry(id, instruction)
        ));
    }
}
