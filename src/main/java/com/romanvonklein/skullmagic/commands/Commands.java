package com.romanvonklein.skullmagic.commands;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class Commands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(DebugCommand::register);
    }
}
