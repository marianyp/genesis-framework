package dev.mariany.genesisframework.instruction;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServerInstructionManager {
    private static final ServerInstructionManager INSTANCE = new ServerInstructionManager();

    private final Map<Identifier, InstructionEntry> instructions = new Object2ObjectOpenHashMap<>();

    public static ServerInstructionManager getInstance() {
        return INSTANCE;
    }

    public Optional<InstructionEntry> find(AdvancementHolder advancementEntry) {
        return this.instructions
                .values()
                .stream()
                .filter(
                        instructionEntry ->
                                instructionEntry.getAdvancementEntry().id().equals(advancementEntry.id())
                )
                .findAny();
    }

    public Collection<InstructionEntry> getInstructions() {
        return this.instructions.values();
    }

    public List<Identifier> getInstructionAdvancementIds() {
        return this.instructions.values()
                                .stream()
                                .map(instructionEntry -> instructionEntry.getAdvancementEntry().id()).toList();
    }

    protected void add(InstructionEntry instructionEntry) {
        this.instructions.put(instructionEntry.getId(), instructionEntry);
    }

    protected void clear() {
        this.instructions.clear();
    }
}
