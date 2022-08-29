package com.romanvonklein.skullmagic.commands;

import java.io.File;
import java.io.FileOutputStream;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;

public class DebugCommands {
    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher, CommandRegistryAccess cra,
            RegistrationEnvironment re) {
        commandDispatcher.register(
                CommandManager.literal("skullmagic").then(CommandManager.literal("debug")
                        .then(CommandManager.literal("clear").executes(DebugCommands::clear))));
        commandDispatcher.register(
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
