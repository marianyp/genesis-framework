package dev.mariany.genesisframework.client.advancement;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.client.GenesisFrameworkClient;
import dev.mariany.genesisframework.event.client.advancement.ClientAdvancementEvents;
import dev.mariany.genesisframework.instruction.InstructionEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.multiplayer.ClientAdvancements;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public final class ClientAdvancementDisplay {
    private ClientAdvancementDisplay() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Client Advancement Display");
        ClientAdvancementEvents.SCREEN_INITIALIZED.register(ClientAdvancementDisplay::selectAgeTab);
        ClientAdvancementEvents.ALLOW_ROOT.register(ClientAdvancementDisplay::shouldAddRoot);
    }

    private static void selectAgeTab(
            Map<AdvancementHolder, AdvancementTab> tabs,
            ClientAdvancements advancements,
            Consumer<AdvancementHolder> selectionListener
    ) {
        if (!GenesisFrameworkClient.getConfig().advancementScreenStartsOnAges) {
            return;
        }

        Optional<AdvancementHolder> optionalAgeRoot = tabs.keySet().stream().filter(AgeEntry::isRoot).findFirst();

        if (optionalAgeRoot.isEmpty()) {
            return;
        }

        AdvancementHolder ageRoot = optionalAgeRoot.get();
        selectionListener.accept(ageRoot);
        advancements.setSelectedTab(ageRoot, true);
    }

    private static boolean shouldAddRoot(AdvancementNode root) {
        return !root.holder().id().equals(InstructionEntry.ROOT_ADVANCEMENT_ID);
    }
}
