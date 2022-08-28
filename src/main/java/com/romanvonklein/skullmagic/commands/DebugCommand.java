package com.romanvonklein.skullmagic.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class DebugCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(
                CommandManager.literal("skullmagic").then(CommandManager.literal("debug")
                        .then(CommandManager.literal("clear").executes(DebugCommand::clear))));
        dispatcher.register(
                CommandManager.literal("skullmagic")
                        .then(CommandManager.literal("debug")
                                .then(CommandManager.literal("output")
                                        .executes(DebugCommand::output))));
    }

    private static int output(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        SkullMagic.LOGGER.info("debugging command executed");
        return 1;
    }

    private static int clear(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        SkullMagic.essenceManager.clear();
        return 1;
    }
}
