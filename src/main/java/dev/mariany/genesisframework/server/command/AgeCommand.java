package dev.mariany.genesisframework.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.advancement.AdvancementHelper;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.age.AgeShareManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class AgeCommand {
    private static final DynamicCommandExceptionType AGE_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(
            id -> Component.translatableEscape("genesisframework.age.ageNotFound", id)
    );

    private AgeCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands
                .literal("age")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(createCacheCommand())
                .then(createGiveCommand())
                .then(createTakeCommand());

        dispatcher.register(builder);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createCacheCommand() {
        LiteralArgumentBuilder<CommandSourceStack> clear = Commands
                .literal("clear")
                .then(
                        Commands.literal("global")
                                .executes(context -> executeClearCache(
                                        context.getSource(),
                                        true
                                ))
                )
                .then(
                        Commands.literal("teams")
                                .executes(context -> executeClearCache(
                                        context.getSource(),
                                        false
                                ))
                );

        return Commands.literal("cache").then(clear);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createGiveCommand() {
        RequiredArgumentBuilder<CommandSourceStack, Identifier> age = Commands
                .argument("age", IdentifierArgument.id())
                .suggests(AgeCommand::suggestAges)
                .executes(context -> executeGive(
                        context.getSource(),
                        EntityArgument.getPlayers(context, "targets"),
                        getAgeEntry(context)
                ));

        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> targets = Commands.argument(
                "targets",
                EntityArgument.players()
        ).then(age);

        return Commands.literal("give").then(targets);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createTakeCommand() {
        RequiredArgumentBuilder<CommandSourceStack, Identifier> age = Commands
                .argument("age", IdentifierArgument.id())
                .suggests(AgeCommand::suggestAges)
                .executes(context -> executeTake(
                        context.getSource(),
                        EntityArgument.getPlayers(context, "targets"),
                        getAgeEntry(context)
                ));

        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> targets = Commands.argument(
                "targets",
                EntityArgument.players()
        ).then(age);

        return Commands.literal("take").then(targets);
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

        source.sendSuccess(
                () -> Component.translatableEscape(
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

        source.sendSuccess(
                () -> Component.translatableEscape(
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

        List<AgeEntry> children = GenesisFramework
                .getServerAgeManager()
                .getAges()
                .stream()
                .filter(otherAge ->
                                otherAge.getAge().requiresParent() &&
                                        otherAge.getAge()
                                                .parent()
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
        Identifier id = IdentifierArgument.getId(context, "age");

        Optional<AgeEntry> optionalAgeEntry = GenesisFramework.getServerAgeManager().get(id);

        if (optionalAgeEntry.isEmpty()) {
            throw AGE_NOT_FOUND_EXCEPTION.create(id);
        }

        return optionalAgeEntry.get();
    }

    private static CompletableFuture<Suggestions> suggestAges(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggestResource(
                GenesisFramework.getServerAgeManager().getAges().stream().map(AgeEntry::getId),
                builder
        );
    }
}
