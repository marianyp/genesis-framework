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
import net.minecraft.resources.Identifier;
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

        Map<Identifier, List<Ingredient>> gatedById = getValuesById(gatedByAge);
        Map<Identifier, List<Ingredient>> lockedById = getValuesById(lockedByAge);
        Map<Identifier, List<ItemTrait>> traitsById = getValuesById(traitsByAge);

        return new AgeItemRestrictions(gatedById, lockedById, traitsById);
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

        Map<Identifier, List<Ingredient>> lockedByAgeId = getValuesById(
                ageManager.getLockedItemsByAge(serverPlayer)
        );

        Map<Identifier, List<ItemTrait>> traitsByAgeId = getValuesById(
                ageManager.getActiveTraits(serverPlayer)
        );

        return new PartialAgeItemRestrictions(null, lockedByAgeId, traitsByAgeId);
    }

    private static <T> Map<Identifier, List<T>> getValuesById(Map<AgeEntry, List<T>> valuesByAge) {
        return valuesByAge
                .entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getId(), Map.Entry::getValue));
    }

    private static boolean isMissingConnection(ServerPlayer serverPlayer) {
        return Objects.isNull(serverPlayer.connection);
    }

    private static void onSync(ServerPlayer serverPlayer) {
        ServerAgeEvents.SYNC.invoker().onSync(serverPlayer);
    }
}
