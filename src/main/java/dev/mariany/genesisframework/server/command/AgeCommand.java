package dev.mariany.genesisframework.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.mariany.genesisframework.advancement.AdvancementHelper;
import dev.mariany.genesisframework.age.Age;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.age.ServerAgeManager;
import dev.mariany.genesisframework.age.AgeShareManager;
import dev.mariany.genesisframework.registry.GFRegistryKeys;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class AgeCommand {
    private static final DynamicCommandExceptionType AGE_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(
            id -> Component.translatableEscape("genesisframework.age.ageNotFound", id)
    );

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
                                                                ResourceKeyArgument.key(GFRegistryKeys.AGE)
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
                                                                ResourceKeyArgument.key(GFRegistryKeys.AGE)
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

        List<AgeEntry> children = ServerAgeManager.getInstance()
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
                GFRegistryKeys.AGE,
                AGE_NOT_FOUND_EXCEPTION
        );
        Optional<AgeEntry> optionalAgeEntry = ServerAgeManager.getInstance().get(registryKey.identifier());

        if (optionalAgeEntry.isEmpty()) {
            throw AGE_NOT_FOUND_EXCEPTION.create(registryKey.identifier());
        } else {
            return optionalAgeEntry.get();
        }
    }
}
