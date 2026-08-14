package dev.mariany.genesisframework.server.command;

import dev.mariany.genesisframework.GenesisFramework;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.mariany.genesisframework.advancement.AdvancementHelper;
import dev.mariany.genesisframework.age.Age;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.age.AgeShareManager;
import dev.mariany.genesisframework.event.server.command.ServerCommandEvents;
import dev.mariany.genesisframework.registry.GFRegistries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Optional;

public final class AgeCommand {
    private static final DynamicCommandExceptionType AGE_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(
            id -> Component.translatableEscape("genesisframework.age.ageNotFound", id)
    );

    private AgeCommand() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Age Command");
        ServerCommandEvents.SUGGEST_REGISTRY_ELEMENTS.register(AgeCommand::suggestAgeIds);
    }

    private static Optional<CompletableFuture<Suggestions>> suggestAgeIds(
            ResourceKey<? extends Registry<?>> key,
            SharedSuggestionProvider.ElementSuggestionType suggestionType,
            SuggestionsBuilder builder,
            CommandContext<?> context
    ) {
        if (key != GFRegistries.AGE) {
            return Optional.empty();
        }

        return Optional.of(SharedSuggestionProvider.suggestResource(
                GenesisFramework.getServerAgeManager().getAges().stream().map(AgeEntry::getId),
                builder
        ));
    }

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        dispatcher.register(
                Commands.literal("age")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("cache")
                                .then(Commands.literal("clear")
                                        .then(Commands.literal("global")
                                                .executes(
                                                        context ->
                                                                executeClearCache(
                                                                        context.getSource(), true
                                                                )
                                                )
                                        )
                                        .then(Commands.literal("teams")
                                                .executes(
                                                        context ->
                                                                executeClearCache(
                                                                        context.getSource(), false
                                                                )
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("give")
                                .then(Commands.argument("targets", EntityArgument.players()).then(
                                                Commands.argument(
                                                                "age",
                                                                ResourceKeyArgument.key(GFRegistries.AGE)
                                                        )
                                                        .executes(context ->
                                                                executeGive(
                                                                        context.getSource(),
                                                                        EntityArgument.getPlayers(
                                                                                context,
                                                                                "targets"
                                                                        ),
                                                                        getAgeEntry(context)
                                                                )
                                                        )
                                        )
                                )
                        )
                        .then(Commands.literal("take")
                                .then(Commands.argument("targets", EntityArgument.players()).then(
                                                Commands.argument(
                                                                "age",
                                                                ResourceKeyArgument.key(GFRegistries.AGE)
                                                        )
                                                        .executes(context ->
                                                                executeTake(
                                                                        context.getSource(),
                                                                        EntityArgument.getPlayers(
                                                                                context,
                                                                                "targets"
                                                                        ),
                                                                        getAgeEntry(context)
                                                                )
                                                        )
                                        )
                                )
                        )
        );
    }

    private static int executeClearCache(CommandSourceStack source, boolean global) {
        ServerLevel level = source.getLevel();
        MinecraftServer server = level.getServer();
        AgeShareManager ageShareManager = AgeShareManager.getServerState(server);

        int cleared = ageShareManager.clear(global);

        source.sendSuccess(
                () -> Component.translatable("commands.genesisframework.age.clear", cleared),
                true
        );

        return 1;
    }

    private static int executeGive(CommandSourceStack source, Collection<ServerPlayer> targets, AgeEntry ageEntry) {
        int successCount = AgeShareManager.progressPlayersToAge(targets, ageEntry);

        source.sendSuccess(() -> Component.translatableEscape(
                        "commands.genesisframework.age.give.success",
                        ageEntry.getId(),
                        successCount
                ),
                true
        );

        return successCount;
    }

    private static int executeTake(CommandSourceStack source, Collection<ServerPlayer> targets, AgeEntry ageEntry) {
        int successCount = (int) targets.stream().filter(player -> takeAge(player, ageEntry)).count();

        source.sendSuccess(() -> Component.translatableEscape(
                        "commands.genesisframework.age.take.success",
                        ageEntry.getId(),
                        successCount
                ),
                true
        );

        return successCount;
    }

    private static boolean takeAge(ServerPlayer player, AgeEntry ageEntry) {
        boolean removed = AdvancementHelper.revokeAdvancement(player, ageEntry.getAdvancementHolder());

        List<AgeEntry> children = GenesisFramework.getServerAgeManager()
                                                  .getAges()
                                                  .stream()
                                                  .filter(otherAge ->
                        otherAge.getAge().requiresParent() && otherAge.getAge().parent()
                                .map(parentId -> parentId.equals(ageEntry.getId()))
                                .orElse(false)
                )
                                                  .toList();

        for (AgeEntry child : children) {
            removed = takeAge(player, child) || removed;
        }

        return removed;
    }

    private static AgeEntry getAgeEntry(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceKey<Age> registryKey = ResourceKeyArgument.getRegistryKey(
                context,
                "age",
                GFRegistries.AGE,
                AGE_NOT_FOUND_EXCEPTION
        );
        Optional<AgeEntry> optionalAgeEntry = GenesisFramework.getServerAgeManager().get(registryKey.identifier());

        if (optionalAgeEntry.isEmpty()) {
            throw AGE_NOT_FOUND_EXCEPTION.create(registryKey.identifier());
        }

        return optionalAgeEntry.get();
    }
}
