package dev.mariany.genesisframework.datagen;

import dev.mariany.genesisframework.advancement.criterion.OpenAdvancementTabCriteria;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.instruction.Instruction;
import dev.mariany.genesisframework.instruction.InstructionEntry;
import dev.mariany.genesisframework.item.GFItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class GFInstructionProvider extends InstructionProvider {
    public GFInstructionProvider(
            FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup
    ) {
        super(output, registryLookup);
    }

    @Override
    public void generateInstructions(RegistryWrapper.WrapperLookup registryLookup, Consumer<InstructionEntry> consumer) {
        Instruction.Builder.create()
                .display(
                        GFItems.AGE_BOOK,
                        Text.translatable("instruction.genesisframework.view_ages"),
                        Text.translatable("instruction.genesisframework.view_ages.description",
                                Text.keybind(MinecraftClient.getInstance().options.advancementsKey.getBoundKeyTranslationKey())
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
