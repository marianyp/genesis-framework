package dev.mariany.genesisframework.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

public final class AdvancementHelper {
    private AdvancementHelper() {
    }

    public static boolean giveAdvancement(ServerPlayer player, AdvancementHolder advancementEntry) {
        PlayerAdvancements tracker = player.getAdvancements();
        AdvancementProgress progress = tracker.getOrStartProgress(advancementEntry);

        if (progress.isDone()) {
            return false;
        }

        for (String criterion : progress.getRemainingCriteria()) {
            tracker.award(advancementEntry, criterion);
        }

        return true;
    }

    public static boolean revokeAdvancement(ServerPlayer player, AdvancementHolder advancementEntry) {
        PlayerAdvancements tracker = player.getAdvancements();
        AdvancementProgress progress = tracker.getOrStartProgress(advancementEntry);

        if (!progress.isDone()) {
            return false;
        }

        for (String criterion : progress.getCompletedCriteria()) {
            tracker.revoke(advancementEntry, criterion);
        }

        return true;
    }
}
