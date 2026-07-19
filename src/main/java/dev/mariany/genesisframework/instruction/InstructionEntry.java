package dev.mariany.genesisframework.instruction;

import dev.mariany.genesisframework.GenesisFramework;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.Identifier;
import java.util.Map;
import java.util.Optional;

public class InstructionEntry {
    public static final String ADVANCEMENT_PREFIX = "instruction/";
    public static final Identifier ROOT_ADVANCEMENT_ID = GenesisFramework.id(ADVANCEMENT_PREFIX + "root");
    public static final Identifier VIEW_AGES_INSTRUCTION_ID = GenesisFramework.id("view_ages");

    private final Identifier id;
    private final Instruction instruction;
    private final AdvancementHolder advancementEntry;

    public InstructionEntry(Identifier id, Instruction instruction) {
        this.id = id;
        this.instruction = instruction;
        this.advancementEntry = createAdvancementEntry(id, instruction);
    }

    private AdvancementHolder createAdvancementEntry(Identifier id, Instruction instruction) {
        return new AdvancementHolder(getAdvancementId(id), createAdvancement(instruction));
    }

    public static Advancement createAdvancement(Instruction instruction) {
        Identifier parent = instruction.parent()
                .map(InstructionEntry::getAdvancementId)
                .orElse(ROOT_ADVANCEMENT_ID);

        Map<String, Criterion<?>> criteria = instruction.criteria();

        AdvancementRequirements requirements = instruction.requirements().isEmpty() ?
                AdvancementRequirements.allOf(criteria.keySet()) :
                instruction.requirements();

        return new Advancement(
                Optional.of(parent),
                createAdvancementDisplay(instruction),
                AdvancementRewards.EMPTY,
                criteria,
                requirements,
                false
        );
    }

    private static Optional<DisplayInfo> createAdvancementDisplay(Instruction instruction) {
        Optional<InstructionDisplay> optionalInstructionDisplay = instruction.display();

        if (optionalInstructionDisplay.isEmpty()) {
            return Optional.empty();
        }

        InstructionDisplay instructionDisplay = optionalInstructionDisplay.get();

        return Optional.of(new DisplayInfo(
                instructionDisplay.icon(),
                instructionDisplay.title(),
                instructionDisplay.description(),
                Optional.empty(),
                AdvancementType.TASK,
                false,
                false,
                false
        ));
    }

    public static Identifier getAdvancementId(Identifier id) {
        return id.withPrefix(ADVANCEMENT_PREFIX);
    }

    public Identifier getId() {
        return this.id;
    }

    public Instruction getInstruction() {
        return this.instruction;
    }

    public AdvancementHolder getAdvancementEntry() {
        return this.advancementEntry;
    }
}
