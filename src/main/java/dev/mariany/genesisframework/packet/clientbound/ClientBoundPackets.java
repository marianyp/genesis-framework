package dev.mariany.genesisframework.packet.clientbound;

import dev.mariany.genesisframework.client.GenesisFrameworkClient;
import dev.mariany.genesisframework.GenesisFramework;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;

public final class ClientBoundPackets {
    private ClientBoundPackets() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Client Bound Packets");

        ClientPlayNetworking.registerGlobalReceiver(
                AgeItemRestrictionsPayload.ID,
                ClientBoundPackets::handleSyncAgeItemRestrictions
        );

        ClientPlayNetworking.registerGlobalReceiver(
                PartialAgeItemRestrictionsPayload.ID,
                ClientBoundPackets::handleUpdateAgeItemRestrictions
        );

        ClientPlayNetworking.registerGlobalReceiver(
                UpdateInstructionsPayload.ID,
                ClientBoundPackets::handleUpdateInstructions
        );

        ClientPlayNetworking.registerGlobalReceiver(
                NotifyAgeLockedPayload.ID,
                ClientBoundPackets::handleNotifyAgeLocked
        );
    }

    private static void handleSyncAgeItemRestrictions(
            AgeItemRestrictionsPayload payload,
            ClientPlayNetworking.Context context
    ) {
        context.client().executeIfPossible(
                () -> GenesisFrameworkClient.getAgeManager().updateItemRestrictions(payload.ageItemRestrictions())
        );
    }

    private static void handleUpdateAgeItemRestrictions(
            PartialAgeItemRestrictionsPayload payload,
            ClientPlayNetworking.Context context
    ) {
        context.client().executeIfPossible(
                () -> GenesisFrameworkClient
                        .getAgeManager()
                        .updateItemRestrictions(payload.ageItemRestrictions())
        );
    }

    private static void handleUpdateInstructions(
            UpdateInstructionsPayload payload,
            ClientPlayNetworking.Context context
    ) {
        context.client().executeIfPossible(
                () -> GenesisFrameworkClient
                        .getInstructionManager()
                        .updateInstructionAdvancements(payload.instructions())
        );
    }

    private static void handleNotifyAgeLocked(NotifyAgeLockedPayload payload, ClientPlayNetworking.Context context) {
        Minecraft client = context.client();
        ToastManager toastManager = client.gui.toastManager();

        if (toastManager.getToast(TutorialToast.class, TutorialToast.NO_TOKEN) != null) {
            return;
        }

        TutorialToast.Icons icons = payload.clickInteraction() ?
                TutorialToast.Icons.RIGHT_CLICK :
                TutorialToast.Icons.SOCIAL_INTERACTIONS;

        toastManager.addToast(
                new TutorialToast(
                        client.font,
                        icons,
                        Component.translatable(
                                "tutorial.genesisframework.age_locked.named",
                                payload.ageMetadata().component(),
                                Component.translatable(payload.restrictedTranslation())
                        ),
                        null,
                        true,
                        5000
                )
        );
    }
}
