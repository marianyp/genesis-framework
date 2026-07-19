package dev.mariany.genesisframework.age;

import dev.mariany.genesisframework.item.ItemTrait;
import dev.mariany.genesisframework.packet.clientbound.UpdateItemTraitsPayload;
import dev.mariany.genesisframework.packet.clientbound.UpdateLockedItemsPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AgeSyncManager {
    public static void syncLockedItems(ServerPlayer player) {
        List<Ingredient> lockedItems = ServerAgeManager.getInstance().getLockedItems(player);
        syncLockedItems(player, lockedItems);
    }

    public static void syncLockedItems(ServerPlayer player, List<Ingredient> lockedItems) {
        ServerPlayNetworking.send(player, new UpdateLockedItemsPayload(lockedItems));
    }

    public static void syncItemTraits(ServerPlayer player) {
        Map<AgeEntry, List<ItemTrait>> traitsByAge = ServerAgeManager.getInstance().getActiveTraits(player);
        Map<String, List<ItemTrait>> traitsByLanguageKey = getTraitsByLanguageKey(traitsByAge);
        syncItemTraits(player, traitsByLanguageKey);
    }

    private static Map<String, List<ItemTrait>> getTraitsByLanguageKey(Map<AgeEntry, List<ItemTrait>> traitsByAge) {
        return traitsByAge
                .entrySet()
                .stream()
                .collect(Collectors.toMap(AgeSyncManager::getAgeEntryLanguageKey, Map.Entry::getValue));
    }

    private static String getAgeEntryLanguageKey(Map.Entry<AgeEntry, List<ItemTrait>> entry) {
        return "age." + entry.getKey().getId().toLanguageKey().replace("/", ".");
    }

    public static void syncItemTraits(ServerPlayer player, Map<String, List<ItemTrait>> traitsByLanguageKey) {
        ServerPlayNetworking.send(player, new UpdateItemTraitsPayload(traitsByLanguageKey));
    }
}
