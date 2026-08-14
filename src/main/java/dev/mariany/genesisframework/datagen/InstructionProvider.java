package dev.mariany.genesisframework.datagen;

import dev.mariany.genesisframework.instruction.Instruction;
import dev.mariany.genesisframework.instruction.InstructionEntry;
import dev.mariany.genesisframework.registry.GFRegistries;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class InstructionProvider extends FabricCodecDataProvider<Instruction> {
    public InstructionProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture, GFRegistries.INSTRUCTION, Instruction.CODEC);
    }

    @Override
    protected void configure(BiConsumer<Identifier, Instruction> provider, HolderLookup.Provider registryLookup) {
        this.generateInstructions(
                registryLookup,
                (instructionEntry) -> provider.accept(
                        instructionEntry.getId(),
                        instructionEntry.getInstruction()
                )
        );
    }

    public abstract void generateInstructions(
            HolderLookup.Provider registryLookup,
            Consumer<InstructionEntry> consumer
    );
}
