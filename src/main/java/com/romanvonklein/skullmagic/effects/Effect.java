package com.romanvonklein.skullmagic.effects;

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public abstract class Effect {
    protected int maxDuration;
    protected int currentDuration;

    public abstract void spawn(MinecraftClient client, String worldkey, List<Vec3d> pos, double spellPower);

    public abstract void despawn(MinecraftClient client);
}
