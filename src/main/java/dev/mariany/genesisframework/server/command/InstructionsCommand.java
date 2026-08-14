package dev.mariany.genesisframework.server.command;

import dev.mariany.genesisframework.GenesisFramework;
import com.mojang.brigadier.CommandDispatcher;
import dev.mariany.genesisframework.advancement.AdvancementHelper;
import dev.mariany.genesisframework.instruction.InstructionEntry;
import dev.mariany.genesisframework.instruction.ServerInstructionManager;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.function.BiFunction;

public final class InstructionsCommand {
    private InstructionsCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("instructions")
                        .then(Commands.literal("restart")
                                      .executes(context -> executeRestart(
                                                        context.getSource()
                                                )
                                      )
                        )
                        .then(Commands.literal("skip")
                                      .executes(context -> executeSkip(
                                                        context.getSource()
                                                )
                                      )
                        )
        );
    }

    private static int executeRestart(CommandSourceStack source) {
        int processed = handleAdvancements(source, AdvancementHelper::revokeAdvancement);

        if (processed <= 0) {
            source.sendFailure(Component.translatable("commands.genesisframework.instructions.reset.failure"));
            return processed;
        }

        source.sendSuccess(
                () -> Component.translatable("commands.genesisframework.instructions.reset", processed),
                false
        );

        return processed;
    }

    private static int executeSkip(CommandSourceStack source) {
        int processed = handleAdvancements(source, AdvancementHelper::giveAdvancement);

        if (processed <= 0) {
            source.sendFailure(Component.translatable("commands.genesisframework.instructions.skip.failure"));
            return processed;
        }

        source.sendSuccess(
                () -> Component.translatable("commands.genesisframework.instructions.skip", processed),
                false
        );
        return processed;
    }

    private static int handleAdvancements(
            CommandSourceStack source,
            BiFunction<ServerPlayer, AdvancementHolder, Boolean> consumer
    ) {
        if (!(source.getEntity() instanceof ServerPlayer serverPlayer)) {
            return 0;
        }

        ServerInstructionManager serverInstructionManager = GenesisFramework.getServerInstructionManager();
        Collection<InstructionEntry> instructions = serverInstructionManager.getInstructions();
        int processed = 0;

        for (InstructionEntry instruction : instructions) {
            if (!consumer.apply(serverPlayer, instruction.getAdvancementHolder())) {
                continue;
            }

            ++processed;
        }

        return processed;
    }
}
