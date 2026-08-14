package dev.mariany.genesisframework.instruction;

import dev.mariany.genesisframework.advancement.DynamicAdvancementManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.advancements.*;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServerInstructionManager extends DynamicAdvancementManager {
    private final Map<Identifier, InstructionEntry> instructions = new Object2ObjectOpenHashMap<>();

    @Override
    protected AdvancementHolder createRootAdvancement() {
        return new AdvancementHolder(
                InstructionEntry.ROOT_ADVANCEMENT_ID,
                new Advancement(
                        Optional.empty(),
                        Optional.of(
                                new DisplayInfo(
                                        new ItemStackTemplate(Items.COMPASS),
                                        Component.translatable("advancements.genesisframework.instructions.title"),
                                        Component.empty(),
                                        Optional.of(
                                                new ClientAsset.ResourceTexture(
                                                        Identifier.withDefaultNamespace("block/lime_terracotta")
                                                )
                                        ),
                                        AdvancementType.TASK,
                                        false,
                                        false,
                                        false
                                )
                        ),
                        AdvancementRewards.EMPTY,
                        Map.of(
                                "root",
                                new Criterion<>(
                                        CriteriaTriggers.TICK,
                                        PlayerTrigger.TriggerInstance.tick().triggerInstance()
                                )
                        ),
                        AdvancementRequirements.allOf(List.of("root")),
                        false
                )
        );
    }

    @Override
    protected List<AdvancementHolder> getAdvancements() {
        return this.getInstructions().stream().map(InstructionEntry::getAdvancementHolder).toList();
    }

    public Optional<InstructionEntry> find(AdvancementHolder advancementEntry) {
        return this.instructions
                .values()
                .stream()
                .filter(
                        instructionEntry ->
                                instructionEntry.getAdvancementHolder().id().equals(advancementEntry.id())
                )
                .findAny();
    }

    public Collection<InstructionEntry> getInstructions() {
        return this.instructions.values();
    }

    public List<Identifier> getInstructionAdvancementIds() {
        return this.instructions.values()
                                .stream()
                                .map(instructionEntry -> instructionEntry.getAdvancementHolder().id()).toList();
    }

    protected void add(InstructionEntry instructionEntry) {
        this.instructions.put(instructionEntry.getId(), instructionEntry);
    }

    protected void clear() {
        this.instructions.clear();
    }
}
