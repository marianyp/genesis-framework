package dev.mariany.genesisframework.event.server.advancement;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.age.ServerAgeManager;
import dev.mariany.genesisframework.instruction.InstructionEntry;
import dev.mariany.genesisframework.instruction.ServerInstructionManager;
import dev.mariany.genesisframework.item.GFItems;
import net.minecraft.advancements.*;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.util.*;

public class BeforeAdvancementsLoadHandler {
    public static void beforeAdvancementsLoad(Map<Identifier, Advancement> advancementMap) {
        loadAgeAdvancements(advancementMap);
    }

    private static void loadAgeAdvancements(Map<Identifier, Advancement> advancementMap) {
        ServerAgeManager serverAgeManager = ServerAgeManager.getInstance();
        ServerInstructionManager serverInstructionManager = ServerInstructionManager.getInstance();

        List<AdvancementHolder> advancements = getAdvancementEntries(serverAgeManager, serverInstructionManager);

        for (AdvancementHolder advancementEntry : advancements) {
            advancementMap.put(advancementEntry.id(), advancementEntry.value());
        }

        GenesisFramework.LOGGER.info("Dynamically added {} age advancements successfully!", advancements.size());
    }

    private static List<AdvancementHolder> getAdvancementEntries(
            ServerAgeManager serverAgeManager,
            ServerInstructionManager serverInstructionManager
    ) {
        Collection<AgeEntry> ages = serverAgeManager.getAges();
        Collection<InstructionEntry> instructions = serverInstructionManager.getInstructions();
        List<AdvancementHolder> advancements = new ArrayList<>();

        advancements.add(getRootAgeAdvancement());

        advancements.add(getRootInstructionAdvancement());

        for (AgeEntry ageEntry : ages) {
            advancements.add(ageEntry.getAdvancementHolder());
        }

        for (InstructionEntry instructionEntry : instructions) {
            advancements.add(instructionEntry.getAdvancementEntry());
        }

        return advancements;
    }

    private static AdvancementHolder getRootAgeAdvancement() {
        return new AdvancementHolder(
                AgeEntry.ROOT_ADVANCEMENT_ID,
                new Advancement(
                        Optional.empty(),
                        Optional.of(
                                new DisplayInfo(
                                        new ItemStackTemplate(GFItems.AGE_BOOK),
                                        Component.translatable("advancements.genesisframework.ages.title"),
                                        Component.empty(),
                                        Optional.of(
                                                new ClientAsset.ResourceTexture(
                                                        Identifier.withDefaultNamespace("block/dark_oak_planks")
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
                                        CriteriaTriggers.TICK, PlayerTrigger.TriggerInstance.tick().triggerInstance()
                                )
                        ),
                        AdvancementRequirements.allOf(List.of("root")),
                        false
                )
        );
    }

    private static AdvancementHolder getRootInstructionAdvancement() {
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
                                        CriteriaTriggers.TICK, PlayerTrigger.TriggerInstance.tick().triggerInstance()
                                )
                        ),
                        AdvancementRequirements.allOf(List.of("root")),
                        false
                )
        );
    }
}
