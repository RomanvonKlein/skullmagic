package com.romanvonklein.skullmagic.spells;

import java.util.Arrays;

import com.romanvonklein.skullmagic.effects.Effects;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TeleportSpell extends SpellWithHoldAction {

    protected TeleportSpell() {
        super(1000, 800, 30, true);
    }

    @Override
    public boolean serverAction(ServerPlayerEntity player, double powerLevel) {
        boolean success = true;
        return success;
    }

    @Override
    public boolean clientAction(ClientPlayerEntity player, double powerLevel) {

        boolean success = false;
        HitResult result = player.raycast(25.0 + powerLevel * 25.0, 1, false);
        if (result != null) {
            Vec3d center = result.getPos();
            Effects.POSITION_HEIGHLIGH_EFFECT.spawn(MinecraftClient.getInstance(),
                    player.getWorld().getRegistryKey().toString(), Arrays.asList(center),
                    powerLevel);
            success = true;
        }

        return success;
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        boolean success = false;
        HitResult result = player.raycast(25.0 + powerLevel * 25.0, 1, false);
        if (result != null) {
            Vec3d center = result.getPos();

            World world = player.getWorld();
            world.playSound(null, BlockPos.ofFloored(center),
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1f, 1f);
            player.teleport(center.x, center.y, center.z, true);
            success = true;
        }
        return success;
    }

}
