package dev.mariany.genesisframework.age;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import dev.mariany.genesisframework.event.server.age.ServerAgeEvents;
import dev.mariany.genesisframework.item.trait.ItemTrait;
import dev.mariany.genesisframework.packet.clientbound.AgeItemRestrictionsPayload;
import dev.mariany.genesisframework.packet.clientbound.PartialAgeItemRestrictionsPayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class AgeSyncManager {
    private AgeSyncManager() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Age Sync Manager");
        ServerPlayerEvents.JOIN.register(AgeSyncManager::syncAll);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(AgeSyncManager::onDataPackContentsSync);
        ServerAdvancementEvents.COMPLETION_UPDATED.register(AgeSyncManager::onAdvancementCompletionUpdated);
    }

    private static void onDataPackContentsSync(ServerPlayer serverPlayer, boolean joined) {
        if (joined) {
            return;
        }

        syncAll(serverPlayer);
    }

    private static void onAdvancementCompletionUpdated(ServerPlayer serverPlayer, AdvancementHolder advancementHolder) {
        if (!isAgeAdvancement(advancementHolder)) {
            return;
        }

        syncPartial(serverPlayer);
    }

    private static boolean isAgeAdvancement(AdvancementHolder advancementHolder) {
        return GenesisFramework.getServerAgeManager().find(advancementHolder).isPresent();
    }

    private static void syncAll(ServerPlayer serverPlayer) {
        if (!sendItemRestrictions(serverPlayer)) {
            return;
        }

        onSync(serverPlayer);
    }

    private static boolean sendItemRestrictions(ServerPlayer serverPlayer) {
        if (isMissingConnection(serverPlayer)) {
            return false;
        }

        AgeItemRestrictions ageItemRestrictions = getAgeItemRestrictions(serverPlayer);

        ServerPlayNetworking.send(serverPlayer, new AgeItemRestrictionsPayload(ageItemRestrictions));

        return true;
    }

    private static AgeItemRestrictions getAgeItemRestrictions(ServerPlayer serverPlayer) {
        ServerAgeManager ageManager = GenesisFramework.getServerAgeManager();

        Map<AgeEntry, List<Ingredient>> gatedByAge = ageManager.getGatedItemsByAge();
        Map<AgeEntry, List<Ingredient>> lockedByAge = ageManager.getLockedItemsByAge(serverPlayer);
        Map<AgeEntry, List<ItemTrait>> traitsByAge = ageManager.getActiveTraits(serverPlayer);

        Map<AgeMetadata, List<Ingredient>> gatedByMetadata = getValuesByMetadata(gatedByAge);
        Map<AgeMetadata, List<Ingredient>> lockedByMetadata = getValuesByMetadata(lockedByAge);
        Map<AgeMetadata, List<ItemTrait>> traitsByMetadata = getValuesByMetadata(traitsByAge);

        return new AgeItemRestrictions(gatedByMetadata, lockedByMetadata, traitsByMetadata);
    }

    private static void syncPartial(ServerPlayer serverPlayer) {
        if (!sendPartialItemRestrictions(serverPlayer)) {
            return;
        }

        onSync(serverPlayer);
    }

    private static boolean sendPartialItemRestrictions(ServerPlayer serverPlayer) {
        if (isMissingConnection(serverPlayer)) {
            return false;
        }

        PartialAgeItemRestrictions partialAgeItemRestrictions = getPartialAgeItemRestrictions(serverPlayer);

        ServerPlayNetworking.send(serverPlayer, new PartialAgeItemRestrictionsPayload(partialAgeItemRestrictions));

        return true;
    }

    private static PartialAgeItemRestrictions getPartialAgeItemRestrictions(ServerPlayer serverPlayer) {
        ServerAgeManager ageManager = GenesisFramework.getServerAgeManager();

        Map<AgeMetadata, List<Ingredient>> lockedByAgeName = getValuesByMetadata(
                ageManager.getLockedItemsByAge(serverPlayer)
        );

        Map<AgeMetadata, List<ItemTrait>> traitsByAgeName = getValuesByMetadata(
                ageManager.getActiveTraits(serverPlayer)
        );

        return new PartialAgeItemRestrictions(null, lockedByAgeName, traitsByAgeName);
    }

    private static <T> Map<AgeMetadata, List<T>> getValuesByMetadata(Map<AgeEntry, List<T>> valuesByAge) {
        return valuesByAge
                .entrySet()
                .stream()
                .collect(Collectors.toMap(AgeSyncManager::getAgeMetaData, Map.Entry::getValue));
    }

    private static AgeMetadata getAgeMetaData(Map.Entry<AgeEntry, ?> entry) {
        return entry.getKey().getMetaData();
    }

    private static boolean isMissingConnection(ServerPlayer serverPlayer) {
        return Objects.isNull(serverPlayer.connection);
    }

    private static void onSync(ServerPlayer serverPlayer) {
        ServerAgeEvents.SYNC.invoker().onSync(serverPlayer);
    }
}
