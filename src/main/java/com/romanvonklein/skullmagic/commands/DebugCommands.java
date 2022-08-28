package com.romanvonklein.skullmagic.commands;

import java.io.File;
import java.io.FileOutputStream;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class DebugCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(
                CommandManager.literal("skullmagic").then(CommandManager.literal("debug")
                        .then(CommandManager.literal("clear").executes(DebugCommands::clear))));
        dispatcher.register(
                CommandManager.literal("skullmagic")
                        .then(CommandManager.literal("debug")
                                .then(CommandManager.literal("output")
                                        .executes(DebugCommands::output))));
    }

    private static int output(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        SkullMagic.LOGGER.info("debugging command executed");
        String jsonString = SkullMagic.essenceManager.toJsonString();
        String dir = "./debug/";
        String fileName = "output.json";
        String path = dir + fileName;
        File directory = new File(dir);
        System.out.print(jsonString);
        try {
            if (!directory.exists()) {
                directory.mkdir();
            }
            FileOutputStream outputStream;
            outputStream = new FileOutputStream(path);
            byte[] strToBytes = jsonString.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private static int clear(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        SkullMagic.essenceManager.clear();
        return 1;
    }
}
