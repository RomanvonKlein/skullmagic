package com.romanvonklein.skullmagic.networking;

import java.util.List;

import com.romanvonklein.skullmagic.SkullMagic;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ServerPackageSender {

    public static void sendUpdatePlayerDataPackageForPlayer(ServerPlayerEntity player) {
        if (player != null) {
            PacketByteBuf buf = PacketByteBufs.create();
            NbtCompound nbtCompound = SkullMagic.getServerData().getNbtCompoundForPlayer(player);
            buf.writeNbt(nbtCompound);
            ServerPlayNetworking.send(player, NetworkingConstants.UPDATE_PLAYER_DATA, buf);
        }
    }

    public static void sendEffectPackageToPlayers(List<ServerPlayerEntity> players, String spellname, double power,
            RegistryKey<World> worldKey, Vec3d pos) {
        for (ServerPlayerEntity player : players) {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putString("worldkey", worldKey.toString());
            nbtCompound.putDouble("power", power);
            nbtCompound.putString("spellname", spellname);
            nbtCompound.putDouble("x", pos.getX());
            nbtCompound.putDouble("y", pos.getY());
            nbtCompound.putDouble("z", pos.getZ());

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeNbt(nbtCompound);

            ServerPlayNetworking.send(player, NetworkingConstants.EFFECT_EVENT, buf);
        }
    }
}
