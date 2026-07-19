package dev.mariany.genesisframework.event.server.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.mariany.genesisframework.server.command.AgeCommand;
import dev.mariany.genesisframework.server.command.InstructionsCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandRegistrationHandler {
    public static void onRegister(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext registryAccess,
            Commands.CommandSelection environment
    ) {
        AgeCommand.register(dispatcher);
        InstructionsCommand.register(dispatcher);
    }
}
