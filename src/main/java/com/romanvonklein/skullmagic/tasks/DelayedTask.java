package com.romanvonklein.skullmagic.tasks;

import java.util.UUID;

import org.apache.commons.lang3.function.TriFunction;

import net.minecraft.server.MinecraftServer;

public class DelayedTask {
    public int delayRemaining;
    private Object[] data;
    private UUID playerID;

    private TriFunction<Object[], MinecraftServer, UUID, Boolean> actionFunc;
    public String name;

    public DelayedTask(String name, int ticks, TriFunction<Object[], MinecraftServer, UUID, Boolean> triFunction,
            Object[] data, UUID playerID) {
        this.name = name;
        this.delayRemaining = ticks;
        this.data = data;
        this.actionFunc = triFunction;
        this.playerID = playerID;
    }

    public boolean action(MinecraftServer server) {
        return this.actionFunc.apply(this.data, server, this.playerID);
    }
}
