package dev.mariany.genesisframework.datagen;

import dev.mariany.genesisframework.advancement.criterion.OpenAdvancementTabCriteria;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.instruction.Instruction;
import dev.mariany.genesisframework.instruction.InstructionEntry;
import dev.mariany.genesisframework.item.GFItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class GFInstructionProvider extends InstructionProvider {
    public GFInstructionProvider(
            FabricPackOutput output,
            CompletableFuture<HolderLookup.Provider> registryLookup
    ) {
        super(output, registryLookup);
    }

    @Override
    public void generateInstructions(HolderLookup.Provider registryLookup, Consumer<InstructionEntry> consumer) {
        Instruction.Builder.create()
                .display(
                        GFItems.AGE_BOOK,
                        Component.translatable("instruction.genesisframework.view_ages"),
                        Component.translatable("instruction.genesisframework.view_ages.description",
                                Component.keybind(Minecraft.getInstance().options.keyAdvancements.saveString())
                        )
                )
                .criterion("view_ages", OpenAdvancementTabCriteria.Conditions.create(AgeEntry.ROOT_ADVANCEMENT_ID))
                .build(consumer, InstructionEntry.VIEW_AGES_INSTRUCTION_ID);
    }

    @Override
    public String getName() {
        return "Genesis Framework Instructions";
    }
}
