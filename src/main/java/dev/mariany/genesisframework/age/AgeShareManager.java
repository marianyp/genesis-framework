package dev.mariany.genesisframework.age;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.advancement.AdvancementHelper;
import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import dev.mariany.genesisframework.gamerule.GFGameRules;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class AgeShareManager extends SavedData {
    private static final SavedDataType<AgeShareManager> STATE_TYPE = new SavedDataType<>(
            GenesisFramework.id("ages"),
            AgeShareManager::new,
            Packed.CODEC.xmap(AgeShareManager::new, AgeShareManager::pack),
            DataFixTypes.LEVEL
    );

    private final Set<Identifier> globalAges = new HashSet<>();
    private final Map<String, Set<Identifier>> teamAges = new HashMap<>();

    public AgeShareManager() {
        this.setDirty();
    }

    public AgeShareManager(Packed packed) {
        this.unpack(packed);
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Age Share Manager");
        ServerPlayConnectionEvents.JOIN.register(AgeShareManager::onPlayerJoin);
        ServerAdvancementEvents.AWARDED.register(AgeShareManager::onAdvancementAwarded);
        GameRuleEvents.changeCallback(GFGameRules.AGE_SHARING).register(AgeShareManager::onGameRuleChange);
    }

    public static void onPlayerJoin(
            ServerGamePacketListenerImpl serverPlayNetworkHandler,
            PacketSender packetSender,
            MinecraftServer server
    ) {
        ServerPlayer serverPlayer = serverPlayNetworkHandler.player;
        GenesisFramework.LOGGER.info("Preparing to apply shared ages to {}", serverPlayer);
        getServerState(server).applySharedAges(serverPlayer);
    }

    private static void onAdvancementAwarded(ServerPlayer player, AdvancementHolder advancement) {
        GenesisFramework.getServerAgeManager()
                        .find(advancement)
                        .filter(ageEntry -> ageEntry.isDone(player))
                        .ifPresent(ageEntry -> onAdvancementAwarded(player, ageEntry));
    }

    private static void onAdvancementAwarded(ServerPlayer player, AgeEntry ageEntry) {
        ServerLevel serverLevel = player.level();
        MinecraftServer server = serverLevel.getServer();
        GameRules gameRules = server.getGameRules();
        AgeSharingOption ageSharingOption = gameRules.get(GFGameRules.AGE_SHARING);

        if (ageSharingOption == AgeSharingOption.DISABLED) {
            return;
        }

        AgeShareManager ageShareManager = getServerState(server);

        if (ageSharingOption == AgeSharingOption.TEAMS) {
            ageShareManager.shareWithTeam(player, ageEntry);
            return;
        }

        ageShareManager.shareWithServer(server, ageEntry);
    }

    private static void onGameRuleChange(AgeSharingOption ageSharingOption, MinecraftServer server) {
        if (ageSharingOption == AgeSharingOption.DISABLED) {
            return;
        }

        AgeShareManager ageShareManager = getServerState(server);
        Collection<ServerPlayer> players = PlayerLookup.all(server);

        GenesisFramework.LOGGER.info("Preparing to apply shared ages to {} players", players.size());

        for (ServerPlayer player : players) {
            ageShareManager.applySharedAges(player, ageSharingOption);
        }
    }

    public static AgeShareManager getServerState(MinecraftServer server) {
        SavedDataStorage persistentStateManager = server.overworld().getDataStorage();

        AgeShareManager state = persistentStateManager.computeIfAbsent(STATE_TYPE);

        state.setDirty();

        return state;
    }

    public int clear(boolean global) {
        if (global) {
            return this.clearGlobalAges();
        }

        int cleared = this.teamAges.values().stream().mapToInt(Set::size).sum();
        this.teamAges.clear();
        this.setDirty();
        return cleared;
    }

    private int clearGlobalAges() {
        int cleared = this.globalAges.size();
        this.globalAges.clear();
        this.setDirty();
        return cleared;
    }

    public void applySharedAges(ServerPlayer serverPlayer) {
        ServerLevel serverLevel = serverPlayer.level();
        MinecraftServer server = serverLevel.getServer();
        GameRules gameRules = server.getGameRules();
        AgeShareManager.AgeSharingOption ageSharingOption = gameRules.get(GFGameRules.AGE_SHARING);
        applySharedAges(serverPlayer, ageSharingOption);
    }

    private void applySharedAges(ServerPlayer serverPlayer, AgeShareManager.AgeSharingOption ageSharingOption) {
        if (ageSharingOption == AgeShareManager.AgeSharingOption.DISABLED) {
            return;
        }

        ServerAgeManager serverAgeManager = GenesisFramework.getServerAgeManager();
        Set<Identifier> agesToApply = new HashSet<>(this.globalAges);

        this.addTeamAges(serverPlayer, ageSharingOption, agesToApply);

        for (Identifier ageId : agesToApply) {
            serverAgeManager.get(ageId).ifPresent(ageEntry -> progressPlayerToAge(serverPlayer, ageEntry));
        }
    }

    private void addTeamAges(
            ServerPlayer player,
            AgeSharingOption ageSharingOption,
            Set<Identifier> ages
    ) {
        if (ageSharingOption != AgeSharingOption.TEAMS) {
            return;
        }

        PlayerTeam team = player.getTeam();

        if (team == null) {
            return;
        }

        ages.addAll(this.teamAges.getOrDefault(team.getName(), Set.of()));
    }

    public void shareWithServer(MinecraftServer server, AgeEntry ageEntry) {
        this.globalAges.add(ageEntry.getId());
        this.setDirty();

        List<ServerPlayer> sharingPlayers = getSharingPlayers(server);

        GenesisFramework.LOGGER.info("Preparing to share ages with {} players", sharingPlayers.size());

        progressPlayersToAge(sharingPlayers, ageEntry);
    }

    public void shareWithTeam(ServerPlayer player, AgeEntry ageEntry) {
        PlayerTeam team = player.getTeam();

        if (team == null) {
            return;
        }

        String teamName = team.getName();
        Set<Identifier> ages = teamAges.getOrDefault(teamName, new HashSet<>());
        ages.add(ageEntry.getId());

        teamAges.put(teamName, ages);
        this.setDirty();

        List<ServerPlayer> sharingPlayers = getSharingPlayers(player.level().getServer(), team);

        GenesisFramework.LOGGER.info(
                "Preparing to shared ages with {} players on team {}",
                sharingPlayers.size(),
                teamName
        );

        progressPlayersToAge(sharingPlayers, ageEntry);
    }

    private static List<ServerPlayer> getSharingPlayers(MinecraftServer server) {
        return getSharingPlayers(server, null);
    }

    private static List<ServerPlayer> getSharingPlayers(MinecraftServer server, @Nullable PlayerTeam team) {
        if (team == null) {
            return server.getPlayerList().getPlayers();
        }

        PlayerList playerManager = server.getPlayerList();
        List<ServerPlayer> players = playerManager.getPlayers();

        return players.stream()
                      .filter(serverPlayer -> serverPlayer.getTeam() == team)
                      .toList();
    }

    public static void progressPlayerToAge(ServerPlayer player, AgeEntry ageEntry) {
        progressPlayersToAge(List.of(player), ageEntry);
    }

    public static int progressPlayersToAge(Collection<ServerPlayer> players, AgeEntry ageEntry) {
        progressPlayersToParentAge(players, ageEntry);
        int progressed = 0;

        for (ServerPlayer player : players) {
            if (!AdvancementHelper.giveAdvancement(player, ageEntry.getAdvancementHolder())) {
                continue;
            }

            ++progressed;
        }

        return progressed;
    }

    private static void progressPlayersToParentAge(Collection<ServerPlayer> players, AgeEntry ageEntry) {
        if (!ageEntry.getAge().requiresParent()) {
            return;
        }

        Optional<Identifier> parentAgeId = ageEntry.getAge().parent();

        if (parentAgeId.isEmpty()) {
            return;
        }

        GenesisFramework.getServerAgeManager()
                        .get(parentAgeId.get())
                        .ifPresent(parent -> progressPlayersToAge(players, parent));
    }

    public void unpack(Packed packed) {
        this.globalAges.addAll(packed.globalAges());
        packed.teamAges().forEach((team, ages) -> this.teamAges.put(team, new HashSet<>(ages)));
    }

    public Packed pack() {
        List<Identifier> globalAges = this.globalAges.stream().toList();

        Map<String, List<Identifier>> teamAges = this.teamAges
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> new ArrayList<>(entry.getValue())
                        )
                );


        return new Packed(
                globalAges,
                teamAges
        );
    }

    public record Packed(
            List<Identifier> globalAges,
            Map<String, List<Identifier>> teamAges
    ) {
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                            Identifier.CODEC
                                                    .listOf()
                                                    .optionalFieldOf("GlobalAges", List.of())
                                                    .forGetter(Packed::globalAges),
                                            Codec.unboundedMap(Codec.STRING, Identifier.CODEC.listOf())
                                                 .optionalFieldOf("TeamAges", Map.of())
                                                 .forGetter(Packed::teamAges)
                                    )
                                    .apply(instance, Packed::new)
        );
    }

    public enum AgeSharingOption implements StringRepresentable {
        DISABLED("disabled"),
        TEAMS("teams"),
        EVERYONE("everyone");

        private final String name;

        AgeSharingOption(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.getName();
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
