package com.romanvonklein.skullmagic.networking;

import com.romanvonklein.skullmagic.SkullMagic;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerPackageSender {

    public static void sendUpdatePlayerDataPackageForPlayer(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        NbtCompound nbtCompound = SkullMagic.getServerData().getNbtCompoundForPlayer(player);
        buf.writeNbt(nbtCompound);
        ServerPlayNetworking.send(player, NetworkingConstants.UPDATE_PLAYER_DATA, buf);
    }
}
