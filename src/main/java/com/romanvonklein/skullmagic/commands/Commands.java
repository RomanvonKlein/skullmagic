package com.romanvonklein.skullmagic.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class Commands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(DebugCommands::register);
    }
}
