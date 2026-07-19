package dev.mariany.genesisframework.server.command;

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

public class InstructionsCommand {
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

        if (processed > 0) {
            source.sendSuccess(
                    () -> Component.translatable("commands.genesisframework.instructions.reset", processed),
                    false
            );
        } else {
            source.sendFailure(Component.translatable("commands.genesisframework.instructions.reset.failure"));
        }

        return processed;
    }

    private static int executeSkip(CommandSourceStack source) {
        int processed = handleAdvancements(source, AdvancementHelper::giveAdvancement);

        if (processed > 0) {
            source.sendSuccess(
                    () -> Component.translatable("commands.genesisframework.instructions.skip", processed),
                    false
            );
        } else {
            source.sendFailure(Component.translatable("commands.genesisframework.instructions.skip.failure"));
        }

        return processed;
    }

    private static int handleAdvancements(
            CommandSourceStack source,
            BiFunction<ServerPlayer, AdvancementHolder, Boolean> consumer
    ) {
        int processed = 0;

        if (source.getEntity() instanceof ServerPlayer serverPlayer) {
            ServerInstructionManager serverInstructionManager = ServerInstructionManager.getInstance();
            Collection<InstructionEntry> instructions = serverInstructionManager.getInstructions();

            for (InstructionEntry instruction : instructions) {
                if (consumer.apply(serverPlayer, instruction.getAdvancementEntry())) {
                    ++processed;
                }
            }
        }

        return processed;
    }
}
