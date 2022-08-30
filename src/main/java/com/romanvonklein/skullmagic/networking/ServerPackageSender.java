package com.romanvonklein.skullmagic.networking;

import com.romanvonklein.skullmagic.SkullMagic;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerPackageSender {
    public static void sendUpdateSpellListPackage(ServerPlayerEntity player) {
        StringBuilder builder = new StringBuilder();
        PacketByteBuf buf = PacketByteBufs.create();
        // String msg = "";
        SkullMagic.spellManager.availableSpells.get(player.getUuid()).entrySet().forEach((entry) -> {
            String appendix = entry.getKey() + ":" + Integer.toString(entry.getValue()) + ";";
            builder.append(appendix);
        });
        buf.writeString(builder.toString());

        ServerPlayNetworking.send(player, NetworkingConstants.UPDATE_SPELL_LIST, buf);
    }
}
