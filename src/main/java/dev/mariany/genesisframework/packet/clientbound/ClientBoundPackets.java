package dev.mariany.genesisframework.packet.clientbound;

import dev.mariany.genesisframework.client.age.ClientAgeManager;
import dev.mariany.genesisframework.client.instruction.ClientInstructionManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;

public class ClientBoundPackets {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(
                UpdateLockedItemsPayload.ID,
                (payload, context) ->
                        context.client().executeIfPossible(
                                () -> ClientAgeManager.getInstance().updateLockedItems(payload.items())
                        )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                UpdateItemTraitsPayload.ID,
                (payload, context) ->
                        context.client().executeIfPossible(
                                () -> ClientAgeManager.getInstance().updateTraits(payload.traitsByLanguageKey())
                        )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                UpdateInstructionsPayload.ID,
                (payload, context) ->
                        context.client()
                               .executeIfPossible(
                                       () -> ClientInstructionManager
                                               .getInstance()
                                               .updateInstructionAdvancements(payload.instructions())
                               )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                NotifyAgeLockedPayload.ID,
                (payload, context) -> {
                    Minecraft client = context.client();
                    ToastManager toastManager = client.gui.toastManager();

                    if (toastManager.getToast(TutorialToast.class, TutorialToast.NO_TOKEN) != null) {
                        return;
                    }

                    toastManager.addToast(new TutorialToast(
                            client.font,
                            payload.clickInteraction() ? TutorialToast.Icons.RIGHT_CLICK :
                                    TutorialToast.Icons.SOCIAL_INTERACTIONS,
                            Component.translatable(
                                    "tutorial.genesisframework.ageLocked",
                                    Component.translatable(payload.ageTranslation()),
                                    Component.translatable("age.genesisframework.age"),
                                    Component.translatable(payload.itemTranslation())
                            ),
                            null,
                            true,
                            5000
                    ));
                }
        );
    }
}
