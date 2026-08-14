package dev.mariany.genesisframework.server.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.mariany.genesisframework.GenesisFramework;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class GFCommands {
    private GFCommands() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Commands");
        CommandRegistrationCallback.EVENT.register(GFCommands::registerCommands);
    }

    private static void registerCommands(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext registryAccess,
            Commands.CommandSelection environment
    ) {
        AgeCommand.register(dispatcher);
        InstructionsCommand.register(dispatcher);
    }
}
