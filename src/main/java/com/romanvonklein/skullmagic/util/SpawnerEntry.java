package com.romanvonklein.skullmagic.util;

public class SpawnerEntry {
    public String command;
    public int weight;
    public int maxDelay;
    public int maxSpawns;
    public int range;

    public SpawnerEntry(String command,
            int weight,
            int maxDelay,
            int maxSpawns,
            int range) {
        this.command = command;
        this.weight = weight;
        this.maxDelay = maxDelay;
        this.maxSpawns = maxSpawns;
        this.range = range;
    }
}