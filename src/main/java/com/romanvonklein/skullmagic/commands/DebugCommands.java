package com.romanvonklein.skullmagic.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.spells.SpellManager;
import com.romanvonklein.skullmagic.util.Parsing;

import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class DebugCommands {
        public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher, boolean dedicated) {
                // debug clear
                commandDispatcher.register(
                                CommandManager.literal("skullmagic").then(CommandManager.literal("debug")
                                                .then(CommandManager.literal("clear").executes(DebugCommands::clear))));
                // debug output
                commandDispatcher.register(
                                CommandManager.literal("skullmagic")
                                                .then(CommandManager.literal("debug")
                                                                .then(CommandManager.literal("output")
                                                                                .executes(DebugCommands::output))));
                // learnspell
                commandDispatcher.register(CommandManager.literal("skullmagic").then(CommandManager.literal("learn")
                                .then((ArgumentBuilder<ServerCommandSource, ?>) CommandManager
                                                .argument("playername", StringArgumentType.word())
                                                .suggests((context, builder) -> CommandSource
                                                                .suggestMatching(
                                                                                ((ServerCommandSource) context
                                                                                                .getSource())
                                                                                                .getServer()
                                                                                                .getPlayerManager()
                                                                                                .getPlayerNames(),
                                                                                builder))
                                                .then((ArgumentBuilder<ServerCommandSource, ?>) CommandManager
                                                                .argument("spellname", StringArgumentType.word())
                                                                .suggests((context, builder) -> CommandSource
                                                                                .suggestMatching(Parsing.setToStringArr(
                                                                                                SpellManager.SpellDict
                                                                                                                .keySet()),
                                                                                                builder))
                                                                .executes(
                                                                                (context) -> DebugCommands.learnSpell(
                                                                                                context,
                                                                                                StringArgumentType
                                                                                                                .getString(context,
                                                                                                                                "playername"),
                                                                                                StringArgumentType
                                                                                                                .getString(context,
                                                                                                                                "spellname")))))));
                // learnall
                commandDispatcher.register(CommandManager.literal("skullmagic")
                                .then(CommandManager.literal("learnall").then(CommandManager
                                                .argument("playername",
                                                                StringArgumentType
                                                                                .word())
                                                .suggests((context, builder) -> CommandSource
                                                                .suggestMatching(
                                                                                ((ServerCommandSource) context
                                                                                                .getSource())
                                                                                                .getServer()
                                                                                                .getPlayerManager()
                                                                                                .getPlayerNames(),
                                                                                builder))
                                                .executes(
                                                                (context) -> DebugCommands.learnAllSpellsForPlayer(
                                                                                context, StringArgumentType
                                                                                                .getString(context,
                                                                                                                "playername"))))));
        }

        private static int learnAllSpellsForPlayer(CommandContext<ServerCommandSource> context, String playername) {
                ServerCommandSource src = context.getSource();
                SkullMagic.spellManager
                                .learnAllSpellsForPlayer(src.getServer().getPlayerManager().getPlayer(playername));
                return 1;
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

        // TODO: this is not really a debug command...
        private static int learnSpell(CommandContext<ServerCommandSource> context, String playername, String spellName)
                        throws CommandSyntaxException {
                ServerCommandSource src = context.getSource();
                SkullMagic.spellManager.learnSpell(src.getServer().getPlayerManager().getPlayer(playername), spellName,
                                true);
                return 1;
        }
}
