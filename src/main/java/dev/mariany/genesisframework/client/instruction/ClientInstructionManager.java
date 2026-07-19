package dev.mariany.genesisframework.client.instruction;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.client.GFClient;
import dev.mariany.genesisframework.client.toast.HideableToast;
import dev.mariany.genesisframework.client.toast.InstructionToast;
import dev.mariany.genesisframework.client.toast.InstructionsCompleteToast;
import dev.mariany.genesisframework.mixin.accessor.ClientAdvancementManagerAccessor;
import dev.mariany.genesisframework.mixin.accessor.ToastManagerAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ClientInstructionManager {
    private static final ClientInstructionManager INSTANCE = new ClientInstructionManager();

    private static final Identifier INSTRUCTIONS_COMPLETE_TOAST_ID = GenesisFramework.id("instructions_complete");

    private static final int DEFAULT_QUEUE_DELAY_MILLISECONDS = 1000;

    private final Set<Identifier> instructionAdvancements = new HashSet<>();
    private final Object2ObjectOpenHashMap<Identifier, HideableToast> toasts = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<Identifier, HideableToast> waitingToasts = new Object2ObjectOpenHashMap<>();

    private boolean complete = true;
    private long queueTargetMilliseconds = -1;

    private ClientInstructionManager() {
    }

    public static ClientInstructionManager getInstance() {
        return INSTANCE;
    }

    public void reset() {
        GenesisFramework.LOGGER.info("Resetting instructions state");

        this.complete = true;
        this.queueTargetMilliseconds = -1;
        this.instructionAdvancements.clear();
        this.waitingToasts.clear();
        this.toasts.forEach((id, toast) -> toast.hide());
        this.toasts.clear();
    }

    public void update() {
        if (this.waitingToasts.isEmpty()) {
            return;
        }

        if (this.queueTargetMilliseconds <= 0 || this.queueTargetMilliseconds > Util.getMillis()) {
            return;
        }

        ToastManager toastManager = Minecraft.getInstance().gui.toastManager();

        for (Map.Entry<Identifier, HideableToast> entry : this.waitingToasts.entrySet()) {
            Identifier id = entry.getKey();
            HideableToast toast = entry.getValue();

            int occupiedSlotCount = toast.occcupiedSlotCount();

            int freeSlotsIndex = ((ToastManagerAccessor) toastManager).genesis$findFreeSlotsIndex(occupiedSlotCount);

            if (freeSlotsIndex != 0) {
                continue;
            }

            this.toasts.put(id, toast);
            toastManager.addToast(toast);
            this.waitingToasts.remove(id);
        }

        if (this.waitingToasts.isEmpty()) {
            this.queueTargetMilliseconds = -1;
        }
    }

    public void updateInstructionAdvancements(Collection<Identifier> changes) {
        if (!GFClient.getConfig().displayInstructionToasts) {
            return;
        }

        int oldSize = this.instructionAdvancements.size();

        this.reset();

        this.instructionAdvancements.addAll(changes);

        GenesisFramework.LOGGER.info(
                "Loaded instructions. Old Size: {} | New Size: {}",
                oldSize,
                this.instructionAdvancements.size()
        );

        this.refreshInstructionToasts();
    }

    public void removeToast(Identifier id) {
        if (this.toasts.containsKey(id)) {
            this.toasts.get(id).hide();
            this.toasts.remove(id);
        }

        this.waitingToasts.remove(id);
    }

    private void resetCompleteToast() {
        this.complete = false;

        HideableToast toast = this.toasts.get(INSTRUCTIONS_COMPLETE_TOAST_ID);

        if (toast != null) {
            toast.hide();
        }

        this.toasts.remove(INSTRUCTIONS_COMPLETE_TOAST_ID);
        this.waitingToasts.remove(INSTRUCTIONS_COMPLETE_TOAST_ID);
    }

    public void refreshInstructionToasts() {
        List<AdvancementNode> instructionAdvancements = getInstructionAdvancements();

        if (instructionAdvancements.isEmpty()) {
            return;
        }

        int toastCount = this.toasts.size();
        int delayMilliseconds = toastCount == 0 ? 0 : DEFAULT_QUEUE_DELAY_MILLISECONDS;

        for (AdvancementNode instructionAdvancement : instructionAdvancements) {
            Identifier id = instructionAdvancement.holder().id();
            Optional<AdvancementProgress> optionalAdvancementProgress = getAdvancementProgress(instructionAdvancement);

            if (optionalAdvancementProgress.isEmpty()) {
                continue;
            }

            AdvancementProgress progress = optionalAdvancementProgress.get();

            boolean isDone = progress.isDone();
            boolean isParentComplete = isParentComplete(instructionAdvancement);

            if (isDone || !isParentComplete) {
                removeToast(id);
            } else if (!this.toasts.containsKey(id)) {
                addToast(instructionAdvancement, delayMilliseconds);
            }
        }

        if (!this.toasts.isEmpty() || !this.waitingToasts.isEmpty()) {
            return;
        }

        if (this.complete) {
            return;
        }

        this.complete = true;

        queueToast(INSTRUCTIONS_COMPLETE_TOAST_ID, new InstructionsCompleteToast(), 0);
    }

    private List<AdvancementNode> getInstructionAdvancements() {
        LocalPlayer localPlayer = Minecraft.getInstance().player;

        if (localPlayer == null) {
            return Collections.emptyList();
        }

        return getInstructionAdvancements(localPlayer);
    }

    private List<AdvancementNode> getInstructionAdvancements(LocalPlayer localPlayer) {
        ClientAdvancements clientAdvancementManager = localPlayer.connection.getAdvancements();
        AdvancementTree advancementTree = clientAdvancementManager.getTree();
        Collection<AdvancementNode> nodes = advancementTree.nodes();
        return nodes.stream().filter(this::isInstructionAdvancement).toList();
    }

    private boolean isInstructionAdvancement(AdvancementNode placedAdvancement) {
        return this.instructionAdvancements.contains(placedAdvancement.holder().id());
    }

    private Optional<AdvancementProgress> getAdvancementProgress(AdvancementNode placedAdvancement) {
        return getAdvancementProgress(placedAdvancement.holder());
    }

    private Optional<AdvancementProgress> getAdvancementProgress(AdvancementHolder advancementEntry) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer clientPlayer = client.player;

        if (clientPlayer != null) {
            ClientAdvancements clientAdvancementManager = clientPlayer.connection.getAdvancements();
            Map<AdvancementHolder, AdvancementProgress> advancementProgresses = (
                    (ClientAdvancementManagerAccessor) clientAdvancementManager
            ).genesis$advancementProgresses();

            if (advancementProgresses.containsKey(advancementEntry)) {
                return Optional.of(advancementProgresses.get(advancementEntry));
            }
        }

        return Optional.empty();
    }

    public void addToast(AdvancementNode placedAdvancement, int delayMilliseconds) {
        Minecraft client = Minecraft.getInstance();
        AdvancementHolder advancementEntry = placedAdvancement.holder();
        Identifier id = advancementEntry.id();
        Advancement advancement = advancementEntry.value();
        Optional<DisplayInfo> optionalAdvancementDisplay = advancement.display();

        removeToast(id);

        optionalAdvancementDisplay.ifPresent(advancementDisplay -> {
            @Nullable Component description = advancementDisplay.getDescription();

            if (description instanceof PlainTextContents plainTextContent) {
                if (plainTextContent.text().isEmpty()) {
                    description = null;
                }
            }

            InstructionToast instructionToast = new InstructionToast(
                    client.font,
                    advancementDisplay.getIcon(),
                    advancementDisplay.getTitle(),
                    description
            );

            queueToast(id, instructionToast, delayMilliseconds);
        });

        resetCompleteToast();
    }

    public void queueToast(Identifier id, HideableToast toast, int delayMilliseconds) {
        this.queueTargetMilliseconds = Util.getMillis() + delayMilliseconds;
        this.waitingToasts.put(id, toast);
    }

    private boolean isParentComplete(AdvancementNode advancement) {
        AdvancementNode parent = advancement.parent();

        if (parent == null) {
            return true;
        }

        Optional<AdvancementProgress> optionalParentProgress = getAdvancementProgress(parent);

        if (optionalParentProgress.isPresent()) {
            AdvancementProgress parentProgress = optionalParentProgress.get();

            if (!parentProgress.isDone()) {
                return false;
            }
        }

        return isParentComplete(parent);
    }
}
